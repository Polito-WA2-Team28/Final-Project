import { Button, Card, Col, Row, Form, Modal } from 'react-bootstrap'
import { useParams } from 'react-router-dom'
import { useContext, useEffect, useState } from 'react'
import '../styles/TicketPage.css'
import { ActionContext, UserContext } from '../Context'
import Roles from '../model/rolesEnum'
import TicketState from '../model/ticketState'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck } from '@fortawesome/free-solid-svg-icons'
import dayjs from 'dayjs'
import { errorToast } from '../components/toastHandler'

export default function TicketPage() {
  const { ticketId } = useParams()
  const [ticket, setTicket] = useState(null)
  const [messages, setMessages] = useState([])
  const [newMessage, setNewMessage] = useState('')
  const [dirty, setDirty] = useState(false)
  const [files, setFiles] = useState([])
  const [filesLabel, setFilesLabel] = useState('')

  const { sendMessage, getMessages, getTicketByID, getAttachment } = useContext(
    ActionContext,
  )
  const { role, experts, user } = useContext(UserContext)

  const myGetMessages = () => {
    getMessages(ticketId).then((messages) => {
      if (messages && messages.content != null) {
        for (let i = 0; i < messages.content.length; i++) {
          if (messages.content[i].attachmentsNames.length !== 0) {
            messages.content[i].attachments = []
            for (
              let j = 0;
              j < messages.content[i].attachmentsNames.length;
              j++
            ) {
              getAttachment(
                ticketId,
                messages.content[i].attachmentsNames[j],
              ).then((attachment) => {
                //messages.content[i].attachments.push(attachment)
                console.log(attachment)
              })
            }
          }
        }
      }
      setMessages(
        messages.content.sort((a, b) => a.timestamp.localeCompare(b.timestamp)),
      )
    })
  }

  const sendNewMessage = () => {
    if (newMessage === '') {
      errorToast('Message cannot be empty')
      return
    }

    sendMessage(ticketId, newMessage, files)
      .then(() => {
        setNewMessage('')
        setFiles([])
        setFilesLabel('')
        myGetMessages()
      })
      .catch((error) => console.error(error))
  }

  const myUpload = (e) => {
    const files = e.target.files
    setFiles(files)
    let label = ''
    for (let i = 0; i < files.length; i++) {
      label += files[i].name + ' '
    }
    setFilesLabel(label)
    console.log(files)
  }

  useEffect(() => {
    console.log('useEffect')
    getTicketByID(ticketId)
      .then((ticket) => setTicket(ticket))
      .then(() => setDirty(false))
  }, [dirty])

  useEffect(() => {
    myGetMessages()
  }, [ticket])

  return (
    <>
      {ticket == null ? (
        <h1>Loading...</h1>
      ) : (
        <Card className="ticketPageCard" style={{ height: '70%' }}>
          <Card.Body>
            <h1>Ticket page</h1>
            <Row style={{ height: '100%' }}>
              <Col style={{ display: 'grid' }}>
                <h4>Ticket Details</h4>
                <Card.Text>
                  <strong>Ticket ID:</strong> {ticket.ticketId}
                </Card.Text>
                <Card.Text>
                  <strong>Ticket State:</strong> {ticket.ticketState}
                </Card.Text>
                <Card.Text>
                  <strong>Description:</strong> {ticket.description}
                </Card.Text>
                <Card.Text>
                  <strong>Serial Number:</strong> {ticket.serialNumber}
                </Card.Text>
                <Row style={{ height: '100%' }}>
                  {role === Roles.CUSTOMER && (
                    <CustomerButton ticket={ticket} setDirty={setDirty} />
                  )}
                  {role === Roles.EXPERT && (
                    <ExpertButton ticket={ticket} setDirty={setDirty} />
                  )}
                  {role === Roles.MANAGER && (
                    <ManagerButton
                      ticket={ticket}
                      experts={experts}
                      setDirty={setDirty}
                    />
                  )}
                </Row>
              </Col>

              {role === Roles.MANAGER && (
                  <Col style={{borderLeft: "2px solid black"}}>
                    <h4>Ticket Details</h4>
                    <Row>
                      <div
                        style={{
                          height: '300px',
                          overflowY: 'auto',
                          textAlign: 'start',
                        }}
                      >
                        {ticket.ticketStateLifecycle.map((state, index) => (
                          <p>
                            {dayjs(state.timestamp).format(
                              'DD/MM/YYYY HH:mm:ss',
                            )}
                            - {state.state}
                          </p>
                        ))}
                      </div>
                    </Row>
                  </Col>
              )}
              <Col style={{ position: 'relative', borderLeft: "2px solid black" }} >
                <h4>Messages</h4>
                <Col
                  style={{
                    overflowY: 'auto',
                    height: '300px',
                    marginBottom: '100px',
                  }}
                >
                  {messages != null && messages.length !== 0 ? (
                    messages.map((message, index) => {
                      return (
                        <div
                          key={index}
                          style={
                            role === Roles.MANAGER ||
                            role === Roles.EXPERT ||
                            message.sender === user.username
                              ? { textAlign: 'right', paddingRight: '20px' }
                              : { textAlign: 'left', paddingLeft: '20px' }
                          }
                        >
                          <p>
                            <strong>{message.sender}</strong>
                            <p>{message.messageText}</p>
                          </p>
                        </div>
                      )
                    })
                  ) : (
                    <Card.Text>No messages yet</Card.Text>
                  )}
                </Col>

                {(role === Roles.CUSTOMER || role === Roles.EXPERT) && (
                  <Row
                    style={{
                      position: 'absolute',
                      bottom: '20px',
                    }}
                  >
                    <Col>
                      <Form onSubmit={(e) => e.preventDefault()}>
                        <Form.Group controlId="formBasicEmail">
                          <Form.Control
                            type="text"
                            placeholder="Enter message"
                            value={newMessage}
                            onChange={(ev) => setNewMessage(ev.target.value)}
                          />
                          <Form.Control
                            name={filesLabel}
                            type="file"
                            multiple
                            onChange={myUpload}
                          />
                        </Form.Group>
                      </Form>
                    </Col>
                    <Col>
                      <Button onClick={sendNewMessage}>Send</Button>
                    </Col>
                  </Row>
                )}
              </Col>
            </Row>
          </Card.Body>
        </Card>
      )}
    </>
  )
}

function CustomerButton(props) {
  const ticket = props.ticket

  const { customerReopenTicket, customerCompileSurvey } = useContext(
    ActionContext,
  )

  const [show, setShow] = useState(false)

  const handleCloseModal = () => setShow(false)

  return (
    <>
      <Modal show={show} onHide={handleCloseModal}>
        <Modal.Header closeButton>
          <Modal.Title>Survey</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group>
              <Form.Label>How would you rate the service?</Form.Label>
              <Form.Control></Form.Control>
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleCloseModal}>
            Cancel
          </Button>
          <Button
            variant="primary"
            onClick={() => {
              customerCompileSurvey(ticket.ticketId, 'survey')
              props.setDirty(true)
              handleCloseModal()
            }}
          >
            Submit
          </Button>
        </Modal.Footer>
      </Modal>
      <Col>
        <Button
          variant="success"
          disabled={ticket.ticketState !== TicketState.RESOLVED}
          onClick={() => {
            setShow(true)
          }}
        >
          Close Ticket
        </Button>
      </Col>

      <Col>
        <Button
          variant="danger"
          disabled={ticket.ticketState !== TicketState.CLOSED}
          onClick={() => {
            customerReopenTicket(ticket.ticketId)
            props.setDirty(true)
          }}
        >
          Reopen Ticket
        </Button>
      </Col>
    </>
  )
}

function ExpertButton(props) {
  const ticket = props.ticket

  const { expertResolveTicket } = useContext(ActionContext)

  return (
    <Col>
      <Button
        variant="success"
        disabled={ticket.ticketState !== TicketState.IN_PROGRESS}
        onClick={() => {
          expertResolveTicket(ticket.ticketId)
          props.setDirty(true)
        }}
      >
        Resolve Ticket
      </Button>
    </Col>
  )
}

function ManagerButton(props) {
  const ticket = props.ticket
  const experts = props.experts

  const [show, setShow] = useState(false)
  const {
    managerHandleCloseTicket,
    managerAssignExpert,
    managerRelieveExpert,
  } = useContext(ActionContext)

  return (
    <>
      <Modal size="xl" show={show} onHide={() => setShow(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Assign Expert</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Row>
            <Col
              lg={1}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Button>{'<'}</Button>
            </Col>
            <Col lg={10}>
              {experts.content.map((expert, index) => {
                return (
                  <Row>
                    <Card style={{ padding: '10px', margin: '10px' }}>
                      <Row>
                        <Col
                          lg={10}
                          style={{
                            display: 'flex',
                            justifyContent: 'start',
                            alignItems: 'center',
                          }}
                        >
                          {expert.email} - Expertise:{' '}
                          {expert.expertiseFields.length === 0
                            ? 'NONE'
                            : expert.expertiseFields.toString()}
                        </Col>
                        <Col
                          lg={2}
                          style={{ display: 'flex', justifyContent: 'end' }}
                        >
                          <Button
                            variant="success"
                            onClick={() => {
                              managerAssignExpert(ticket.ticketId, expert.id)
                              props.setDirty(true)
                              setShow(false)
                            }}
                          >
                            <FontAwesomeIcon icon={faCheck} />
                          </Button>
                        </Col>
                      </Row>
                    </Card>
                  </Row>
                )
              })}
            </Col>
            <Col
              lg={1}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Button>{'>'}</Button>
            </Col>
          </Row>
        </Modal.Body>
      </Modal>
      <Col>
        <Button
          variant="success"
          style={{ height: '60px' }}
          disabled={
            ![
              TicketState.OPEN,
              TicketState.IN_PROGRESS,
              TicketState.REOPENED,
            ].includes(ticket.ticketState)
          }
          onClick={() => {
            managerHandleCloseTicket(ticket)
            props.setDirty(true)
          }}
        >
          Close Ticket
        </Button>
      </Col>

      <Col>
        <Button
          style={{ height: '60px' }}
          variant="primary"
          disabled={ticket.ticketState !== TicketState.OPEN}
          onClick={() => setShow(true)}
        >
          Assign Ticket
        </Button>
      </Col>

      <Col>
        <Button
          style={{ height: '60px' }}
          variant="danger"
          disabled={ticket.ticketState !== TicketState.IN_PROGRESS}
          onClick={() => {
            managerRelieveExpert(ticket.ticketId)
            props.setDirty(true)
          }}
        >
          Relieve expert
        </Button>
      </Col>
    </>
  )
}
