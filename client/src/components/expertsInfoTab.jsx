import { useContext, useState } from 'react'
import { Button, Card, Col, Form, CardGroup, Modal, Row } from 'react-bootstrap'
import { ActionContext, UserContext } from '../Context'
import EmptySearch from './EmptySearch'
import { Pagination } from 'react-bootstrap';

export default function ExpertsInfoTab(props) {
  const [show, setShow] = useState(false)
  const { registerExpert, managerGetExperts } = useContext(ActionContext)
  const expertPage = useContext(UserContext).experts
  console.log(expertPage)
  var experts = expertPage.content
  if (experts == null) experts = []

  var totalPages = expertPage.totalPages;
  var [currentPage, setCurrentPage] = useState(1);

  const handlePageChange = (page) => {
    managerGetExperts(page)
    setCurrentPage(page);
  };

  const renderPaginationItems = () => {
    const items = [];
    let startPage = Math.max(currentPage - 2, 1);
    let endPage = Math.min(startPage + 4, totalPages);
    startPage = Math.max(endPage - 4, 1);

    for (let page = startPage; page <= endPage; page++) {
      items.push(
        <Pagination.Item
          key={page}
          active={page === currentPage}
          onClick={() => {if(page !== currentPage) handlePageChange(page)}}
        >
          {page}
        </Pagination.Item>
      );
    }

    return items;
  };

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
      <CardGroup className="mt-1 justify-content-center">
        {experts == null || experts.length === 0 ? (
          <EmptySearch />
        ) : (
          <Row xs={1}  className="w-50">
            {experts.map((expert) => (
              <Col key={expert.id} className="mb-1 ">
                <ExpertItem expert={expert} />
              </Col>
            ))}
          </Row>
        )}
      </CardGroup>

      <div className="d-flex justify-content-center">
        <Pagination>
          <Pagination.First
            disabled={currentPage === 1}
            onClick={() => handlePageChange(1)}
          />
          <Pagination.Prev
            disabled={currentPage === 1}
            onClick={() => handlePageChange(currentPage - 1)}
          />
          {renderPaginationItems()}
          <Pagination.Next
            disabled={currentPage === totalPages || experts.length === 0}
            onClick={() => handlePageChange(currentPage + 1)}
          />
          <Pagination.Last
            disabled={currentPage === totalPages || experts.length === 0}
            onClick={() => handlePageChange(totalPages - 1)}
          />
        </Pagination>
      </div>
    </>
  )
}

function ExpertItem(props) {

  return (
    <>
      <Card key={props.product} className="h-100">
        <Card.Body>
          <Card.Title>
            <p>{props.expert.username}</p>
          </Card.Title>
          <p>{props.expert.id}</p>
          <p>{props.expert.email}</p>
          <p>{props.expert.expertiseFields}</p>
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
    const expertiseFields = []
    if (mobile) expertiseFields.push('MOBILE')
    if (appliances) expertiseFields.push('APPLIANCES')
    if (printers) expertiseFields.push('PRINTERS')
    if (drones) expertiseFields.push('DRONES')
    if (headsets) expertiseFields.push('HEADSETS')
    if (computers) expertiseFields.push('COMPUTERS')
    if (tablets) expertiseFields.push('TABLETS')
    const expert = { username, email, password, expertiseFields }

    props.registrateExpert(expert)
      .then(() => props.handleClose())
      .catch((error) => console.log(error))
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
