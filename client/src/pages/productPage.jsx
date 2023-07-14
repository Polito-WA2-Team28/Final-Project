import { useContext, useState } from 'react'
import { Button, Card, Form, Modal } from 'react-bootstrap'
import { useNavigate, useParams } from 'react-router-dom'
import { ActionContext } from '../Context'
import { successToast } from '../components/toastHandler'

export default function ProductPage() {
  const { productId } = useParams()
  const [product, setProduct] = useState(null)
  const [show, setShow] = useState(false)
  const { getProductByID, handleCreateTicket } = useContext(ActionContext)

  product == null &&
    getProductByID(productId).then((product) => {
      setProduct(product)
    })

  return (
    <div
      style={{
        justifyContent: 'center',
        padding: '10px',
        alignContent: 'center',
        alignItems: 'center',
      }}
    >
      {product == null ? (
        <h2>Loading</h2>
      ) : (
        <Card
          className="productCard"
          style={{ height: '100%', width: '70%', margin: 'auto' }}
        >
          <h2>{product.model}</h2>
          <Card.Body>
            <h5>Device type</h5>
            <p>{product.deviceType}</p>
            <h5>Serial number</h5>
            <p>{product.serialNumber}</p>
          </Card.Body>
          <Card.Footer>
            <OpenNewTicketModal
              show={show}
              handleClose={() => setShow(false)}
              handleCreate={handleCreateTicket}
              product={product}
            />
            <Button style={{ width: '100%' }} onClick={() => setShow(true)}>
              Open a ticket
            </Button>
          </Card.Footer>
        </Card>
      )}
    </div>
  )
}

function OpenNewTicketModal(props) {
  const [description, setDescription] = useState('')
  const serialNumber = props.product.serialNumber

  const navigate = useNavigate()

  const handleCreate = (event) => {
    event.preventDefault()
    const ticket = { description, serialNumber }
    props
      .handleCreate(ticket)
      .then(() => successToast('Ticket created successfully'))
      .then(() => props.handleClose())
      .then(() => navigate('/dashboard'))
  }

  return (
    <Modal show={props.show} onHide={props.handleClose}>
      <Modal.Header closeButton>
        <Modal.Title>
          Create a ticket for {props.product.serialNumber}
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form onSubmit={(e) => e.preventDefault()}>
          <Form.Label>Description</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter description"
            value={description}
            onChange={(ev) => setDescription(ev.target.value)}
          />
        </Form>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={props.handleClose}>
          Close
        </Button>
        <Button variant="primary" onClick={handleCreate}>
          Create
        </Button>
      </Modal.Footer>
    </Modal>
  )
}
