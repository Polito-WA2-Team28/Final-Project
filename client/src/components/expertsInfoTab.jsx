import { useContext, useState } from "react";
import { Button, Card, Col, Form, Modal, Row } from "react-bootstrap";
import { UserContext } from "../Context";
import EmptySearch from "./EmptySearch";

export default function ExpertsInfoTab(props) {
    
    const [show, setShow] = useState(false);
    const { experts } = useContext(UserContext)

    console.log(experts)

    return(
        <>
            <CreateExpertModal show={show} handleClose={() => setShow(false)} />
            <Row>
                <Button onClick={() => setShow(true)}>Create a new Expert</Button>
            </Row>
            <Col style={{ display: 'flex', flexWrap: 'wrap' }}>
        {experts == undefined || experts.content.length === 0 ? (
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

    console.log(props.expert)
    
    return(
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

    return(
        <Modal show={props.show} onHide={props.handleClose}>
            <Modal.Header>
                <Modal.Title>Create a new Expert</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Form onSubmit={e => e.preventDefault()}>
                    <Form.Group>
                        <Form.Label>Expert Name</Form.Label>
                        <Form.Control type="text" placeholder="Enter expert name" />
                    </Form.Group>
                    <Form.Group>
                        <Form.Label>Expert Email</Form.Label>
                        <Form.Control type="text" placeholder="Enter expert email" />
                    </Form.Group>
                </Form>
            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={props.handleClose}>Close</Button>
                <Button variant="primary" onClick={props.handleClose}>Create</Button>
            </Modal.Footer>
            </Modal>
    )
}