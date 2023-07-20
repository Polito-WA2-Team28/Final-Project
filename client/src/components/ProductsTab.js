import { Button, Card, CardGroup, Modal, Form, Col, Row } from "react-bootstrap";
import EmptySearch from "./EmptySearch";
import { useContext, useState } from "react";
import "../styles/ProductsTab.css"
import { ActionContext, UserContext } from "../Context";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useNavigate } from "react-router-dom";
import { faUpRightAndDownLeftFromCenter } from "@fortawesome/free-solid-svg-icons";

import { Pagination } from 'react-bootstrap';


export function ProductsTab(props) {

  const {customerGetProducts, registerProduct} = useContext(ActionContext)

  const productsPage = useContext(UserContext).products;
  var products = productsPage.content;
  if(products==null) products=[]

  const [show, setShow] = useState(false);
  
  const totalPages = productsPage.totalPages;
  const [currentPage, setCurrentPage] = useState(1);

  const handlePageChange = (selectedPage) => {
    customerGetProducts(selectedPage);
    setCurrentPage(selectedPage);
  };

  const renderPaginationItems = () => {
    const items = [];

    // Calcula los límites de los elementos a mostrar
    let startPage = Math.max(currentPage - 2, 1);
    let endPage = Math.min(startPage + 4, totalPages);
    startPage = Math.max(endPage - 4, 1);

    for (let page = startPage; page <= endPage; page++) {
      items.push(
        <Pagination.Item
          key={page}
          active={page === currentPage}
          onClick={() => {if (page !== currentPage) handlePageChange(page)}}
        >
          {page}
        </Pagination.Item>
      );
    }

    return items;
  };


  return (
    <>
      <Button onClick={() => setShow(true)} className="my-2">Register a new product</Button>
      <RegisterNewProductModal show={show} handleClose={() => setShow(false)}
        handleCreate={registerProduct}
      />
      <CardGroup>
        {products === undefined || products.length === 0 ? (
          <EmptySearch />
        ) : (
          <Row xs={1} md={2} lg={3}  className="mb-3">
            {products.map((product) => (
              <Col key={product.id}>
                <ProductItem product={product} />
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
            disabled={currentPage === totalPages || products.length === 0}
            onClick={() => handlePageChange(currentPage + 1)}
          />
          <Pagination.Last
            disabled={currentPage === totalPages || products.length === 0}
            onClick={() => handlePageChange(totalPages - 1)}
          />
        </Pagination>
      </div>
    </>
  );
}

function ProductItem(props) {

  const navigate = useNavigate();

  return <>
    <Card key={props.product} className="productCard h-100">
      <Card.Body>
        <Card.Title>
          <Row>
            <Col> {props.product.model}</Col>
            <Col className="text-end">
              <Button onClick={() => navigate(`/product/${props.product.id}`)}>
                <FontAwesomeIcon icon={faUpRightAndDownLeftFromCenter} />
              </Button></Col>
          </Row>

        </Card.Title>
        <p>{`Device type: ${props.product.deviceType}`}</p>
        <p>{`Serial Number: ${props.product.serialNumber}`}</p>
      </Card.Body>
    </Card>
  </>
}

function RegisterNewProductModal(props) {

  const [productId, setProductId] = useState("");
  const [serialNumber, setSerialNumber] = useState("");

  const handleCreate = () => {
    const product = {productId, serialNumber }
    props.handleCreate(product);
    props.handleClose();
  }

  return <Modal show={props.show} onHide={props.handleClose} className="custom-modal">
    <Modal.Header closeButton className="custom-modal-header">
      <Modal.Title>Register a new product</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <Form onSubmit={e => e.preventDefault()}>
        <Form.Group className="mb-3" controlId="productId">
          <Form.Label>Product ID</Form.Label>
          <Form.Control type="text" placeholder="Enter product ID" value={productId} onChange={ev => setProductId(ev.target.value)} className="custom-form-control" />
        </Form.Group>
        <Form.Group className="mb-3" controlId="serialNumber">
          <Form.Label>Serial Number</Form.Label>
          <Form.Control type="text" placeholder="Enter serial number" value={serialNumber} onChange={ev => setSerialNumber(ev.target.value)} className="custom-form-control" />
        </Form.Group>
      </Form>
    </Modal.Body>
    <Modal.Footer className="custom-modal-footer">
      <Button variant="secondary" onClick={props.handleClose} className="custom-button">Close</Button>
      <Button variant="primary" onClick={handleCreate} className="custom-button">Create</Button>
    </Modal.Footer>
  </Modal>
}
