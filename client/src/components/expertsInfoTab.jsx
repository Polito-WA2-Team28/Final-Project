import { useContext, useState } from 'react'
import { Button, Card, Col, Form, FormCheck, Modal, Row } from 'react-bootstrap'
import { ActionContext, UserContext } from '../Context'
import EmptySearch from './EmptySearch'

export default function ExpertsInfoTab(props) {
  const [show, setShow] = useState(false)
  const { experts } = useContext(UserContext)
  const { registerExpert } = useContext(ActionContext)

  return (
    <>
      <CreateExpertModal
        show={show}
        handleClose={() => setShow(false)}
        registrateExpert={registerExpert}
      />
      <Row>
        <Button onClick={() => setShow(true)}>Create a new Expert</Button>
      </Row>
      <Col style={{ display: 'flex', flexWrap: 'wrap' }}>
        {experts == null ||
        experts.content == null ||
        experts.content.length === 0 ? (
          <EmptySearch />
        ) : (
          experts.content.map((expert) => (
            <ExpertItem key={expert.id} expert={expert} />
          ))
        )}
      </Col>
    </>
  )
}

function ExpertItem(props) {
  //console.log(props.expert)

  return (
    <>
      <Card
        style={{ minWidth: '300px' }}
        key={props.product}
        className="productCard"
      >
        <Card.Body>
          <Card.Title>
            <p>{props.expert.id}</p>
          </Card.Title>
          <p>{props.expert.email}</p>
          <p>{props.expert.expertiseFields.toString()}</p>
        </Card.Body>
      </Card>
    </>
  )
}

function CreateExpertModal(props) {
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [mobile, setMobile] = useState(false)
  const [appliances, setAppliances] = useState(false)
  const [printers, setPrinters] = useState(false)
  const [drones, setDrones] = useState(false)
  const [headsets, setHeadsets] = useState(false)
  const [computers, setComputers] = useState(false)
  const [tablets, setTablets] = useState(false)

  const handleRegistration = () => {
    console.log('Registration')
    const expertiseFields = []
    if (mobile) expertiseFields.push('MOBILE')
    if (appliances) expertiseFields.push('APPLIANCES')
    if (printers) expertiseFields.push('PRINTERS')
    if (drones) expertiseFields.push('DRONES')
    if (headsets) expertiseFields.push('HEADSETS')
    if (computers) expertiseFields.push('COMPUTERS')
    if (tablets) expertiseFields.push('TABLETS')
    const expert = { username, email, password, expertiseFields }
    props.registrateExpert(expert).then(() => props.handleClose())
  }

  return (
    <Modal show={props.show} onHide={props.handleClose}>
      <Modal.Header>
        <Modal.Title>Create a new Expert</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form onSubmit={(e) => e.preventDefault()}>
          <Form.Group>
            <Form.Label>Expert Username</Form.Label>
            <Form.Control
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              type="text"
              placeholder="Enter expert name"
            />
          </Form.Group>
          <Form.Group>
            <Form.Label>Expert Email</Form.Label>
            <Form.Control
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              type="text"
              placeholder="Enter expert email"
            />
          </Form.Group>
          <Form.Group>
            <Form.Label>Expert Password</Form.Label>
            <Form.Control
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              type="password"
              placeholder="Enter expert password"
            />
          </Form.Group>
          <Form.Group>
            <Form.Label>Expertise Fields</Form.Label>
            <Form.Check
              type="checkbox"
              label="MOBILE"
              checked={mobile}
              onChange={(e) => setMobile(e.target.value)}
            />
            <Form.Check
              checked={appliances}
              type="checkbox"
              label="APPLIANCES"
              onChange={(e) => setAppliances(e.target.value)}
            />
            <Form.Check
              checked={printers}
              onChange={(e) => setPrinters(e.target.value)}
              type='checkbox'
              label='PRINTERS'
            />
            <Form.Check
              checked={drones}
              onChange={(e) => setDrones(e.target.value)}
              type='checkbox'
              label='DRONES'
            />
            <Form.Check
              checked={headsets}
              onChange={(e) => setHeadsets(e.target.value)}
              type='checkbox'
              label='HEADSETS'
            />
            <Form.Check
              checked={computers}
              onChange={(e) => setComputers(e.target.value)}
              type='checkbox'
              label='COMPUTERS'
            />
            <Form.Check
              checked={tablets}
              onChange={(e) => setTablets(e.target.value)}
              type='checkbox'
              label='TABLETS'
            />
          </Form.Group>
        </Form>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={props.handleClose}>
          Close
        </Button>
        <Button variant="primary" onClick={handleRegistration}>
          Create
        </Button>
      </Modal.Footer>
    </Modal>
  )
}
