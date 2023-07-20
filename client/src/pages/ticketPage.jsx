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
import { useInterval } from '../components/customHook'

export default function TicketPage() {
  const { ticketId } = useParams()
  const [ticket, setTicket] = useState(null)
  const [messages, setMessages] = useState([])
  const [newMessage, setNewMessage] = useState('')
  const [dirty, setDirty] = useState(true)
  const [files, setFiles] = useState([])
  const [filesLabel, setFilesLabel] = useState('')
  const [messageDirty, setMessageDirty] = useState(true)
  const [lock, setLock] = useState(false)

  const { sendMessage, getMessages, getTicketByID, getAttachment } = useContext(
    ActionContext,
  )
  const { role, experts, username } = useContext(UserContext)

  const myGetMessages = (noPage) => {
    getMessages(ticketId, noPage).then((messagesParam) => {
      if (
        messagesParam != null &&
        messagesParam.content != null &&
        messagesParam.content.length !== 0
      )
        setMessages(
          messagesParam.content.sort((a, b) =>
            a.timestamp.localeCompare(b.timestamp),
          ),
        )
      scrollToBottom()
    })
  }

  const sendNewMessage = () => {
    if (newMessage === '') {
      errorToast('Message cannot be empty')
      return
    }
    sendMessage(ticketId, newMessage, files).then(() => {
      setNewMessage('')
      setFilesLabel('')
      scrollToBottom()
      setFiles([])
      setMessageDirty(true)
    })
  }

  const scrollToBottom = () => {
    setTimeout(() => {
      const messagesDiv = document.getElementById('messages')
      if (messagesDiv != null) messagesDiv.scrollTop = messagesDiv.scrollHeight
    }, 100)
  }

  const myUpload = (e) => {
    const files = e.target.files
    setFiles(files)
    let label = ''
    for (let i = 0; i < files.length; i++) {
      label += files[i].name + ' '
    }
    setFilesLabel(label)
  }

  useEffect(() => {
    console.log('dirty', dirty)
    if (dirty) {
      getTicketByID(ticketId)
        .then((ticket) => {
          setTicket(ticket)
          console.log('ticket', ticket)
        })
        .then(() => setLock(false))
        .then(() => setDirty(false))
    }
  }, [dirty])

  useEffect(() => {
    if (messageDirty) {
      myGetMessages(1)
      setMessageDirty(false)
    }
  }, [messageDirty])

  useInterval(() => {
    console.log('refreshing messages')
    myGetMessages(1)
  }, 10000)

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
                {ticket.survey && (
                  <Card.Text>
                    <strong>Customer Survey:</strong> {ticket.survey}
                  </Card.Text>
                )}
                <Card.Text>
                  <strong>Expert assigned:</strong>{' '}
                  {ticket.expertEmail || 'NONE'}
                </Card.Text>
                <Row style={{ height: '100%' }}>
                  {role === Roles.CUSTOMER && (
                    <CustomerButton
                      ticket={ticket}
                      setDirty={setDirty}
                      lock={lock}
                      setLock={setLock}
                    />
                  )}
                  {role === Roles.EXPERT && (
                    <ExpertButton
                      ticket={ticket}
                      setDirty={setDirty}
                      lock={lock}
                      setLock={setLock}
                    />
                  )}
                  {role === Roles.MANAGER && (
                    <ManagerButton
                      ticket={ticket}
                      experts={experts}
                      setDirty={setDirty}
                      lock={lock}
                      setLock={setLock}
                    />
                  )}
                </Row>
              </Col>

              {role === Roles.MANAGER && (
                <Col style={{ borderLeft: '2px solid black' }}>
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
                        <p key={state.timestamp + state.state}>
                          {dayjs(state.timestamp).format('DD/MM/YYYY HH:mm:ss')}
                          - {state.state}
                        </p>
                      ))}
                    </div>
                  </Row>
                </Col>
              )}
              <Col
                style={{ position: 'relative', borderLeft: '2px solid black' }}
              >
                <h4>Messages</h4>
                <Col
                  id="messages"
                  style={{
                    overflowY: 'auto',
                    height: '250px',
                    marginBottom: '120px',
                  }}
                >
                  {messages != null && messages.length !== 0 ? (
                    messages.map((message, index) => {
                      return (
                        <div
                          key={index}
                          style={
                            role === Roles.MANAGER ||
                            message.sender === username
                              ? { textAlign: 'right', paddingRight: '20px' }
                              : { textAlign: 'left', paddingLeft: '20px' }
                          }
                        >
                          <p>
                            <strong>{message.sender}</strong>
                            <br />
                            {message.messageText}
                            {message.attachmentsNames &&
                              message.attachmentsNames.length > 0 &&
                              message.attachmentsNames.map(
                                (attachment, index) => (
                                  <>
                                    <br />
                                    <Button
                                      onClick={() =>
                                        getAttachment(
                                          ticket.ticketId,
                                          attachment,
                                        )
                                      }
                                      variant="link"
                                    >
                                      {index + 1} - {attachment}
                                    </Button>
                                  </>
                                ),
                              )}
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
                      bottom: '10px',
                    }}
                  >
                    <Col>
                      <Form
                        onSubmit={(e) => {
                          e.preventDefault()
                          sendNewMessage()
                        }}
                      >
                        <Form.Group controlId="formBasicEmail">
                          <Row>
                            <Form.Control
                              style={{ margin: '10px' }}
                              type="text"
                              placeholder="Enter message"
                              value={newMessage}
                              onChange={(ev) => setNewMessage(ev.target.value)}
                            />
                          </Row>
                          <Row>
                            <Form.Control
                              style={{ margin: '10px' }}
                              name={filesLabel}
                              type="file"
                              multiple
                              onChange={myUpload}
                            />
                          </Row>
                        </Form.Group>
                      </Form>
                    </Col>
                    <Col
                      style={{
                        display: 'flex',
                        justifyContent: 'end',
                        alignItems: 'center',
                      }}
                    >
                        <Button
                          disabled={newMessage === ''}
                        onClick={sendNewMessage}
                        style={{ height: '80%', width: '60%' }}
                      >
                        Send
                      </Button>
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
  const [survey, setSurvey] = useState('')

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
              <Form.Control
                type="text"
                value={survey}
                onChange={(ev) => setSurvey(ev.target.value)}
              />
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
              props.setLock(true)
              customerCompileSurvey(ticket.ticketId, survey)
                .then(() => {props.setDirty(true); handleCloseModal()})
            }}
          >
            Submit
          </Button>
        </Modal.Footer>
      </Modal>
      <Col>
        <Button
          variant="success"
          disabled={ticket.ticketState !== TicketState.RESOLVED || props.lock}
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
          disabled={ticket.ticketState !== TicketState.CLOSED || props.lock}
          onClick={() => {
            props.setLock(true)
            customerReopenTicket(ticket.ticketId)
            .then(() => props.setDirty(true))
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
        disabled={ticket.ticketState !== TicketState.IN_PROGRESS || props.lock}
        onClick={() => {
          props.setLock(true)
          expertResolveTicket(ticket.ticketId)
          .then(() => props.setDirty(true))
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
    getExpertsPage,
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
              <Button
                disabled={experts.currentPage === 1}
                onClick={() => getExpertsPage(experts.currentPage - 1)}
              >
                {'<'}
              </Button>
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
                              props.setLock(true)
                              managerAssignExpert(ticket.ticketId, expert.id)
                                .then(() => props.setDirty(true))
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
              <Button
                disabled={experts.currentPage === experts.totalPages}
                onClick={() => getExpertsPage(experts.currentPage + 1)}
              >
                {'>'}
              </Button>
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
              TicketState.RESOLVED,
              TicketState.REOPENED,
            ].includes(ticket.ticketState) || props.lock
          }
          onClick={() => {
            props.setLock(true)
            managerHandleCloseTicket(ticket.ticketId)
              .then(() => props.setDirty(true))
          }}
        >
          Close Ticket
        </Button>
      </Col>

      <Col>
        <Button
          style={{ height: '60px' }}
          variant="primary"
          disabled={ticket.ticketState !== TicketState.OPEN || props.lock}
          onClick={() => setShow(true)}
        >
          Assign Ticket
        </Button>
      </Col>

      <Col>
        <Button
          style={{ height: '60px' }}
          variant="danger"
          disabled={
            ticket.ticketState !== TicketState.IN_PROGRESS || props.lock
          }
          onClick={() => {
            props.setLock(true)
            managerRelieveExpert(ticket.ticketId)
              .then(() => props.setDirty(true))
          }}
        >
          Relieve expert
        </Button>
      </Col>
    </>
  )
}
