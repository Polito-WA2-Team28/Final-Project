import { Button, Card, CardGroup, Modal, Form, Col, Row } from "react-bootstrap";
import EmptySearch from "./EmptySearch";
import { useContext, useState } from "react";
import "../styles/ProductsTab.css"
import { UserContext } from "../Context";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useNavigate } from "react-router-dom";
import { faUpRightAndDownLeftFromCenter } from "@fortawesome/free-solid-svg-icons";

import { Pagination } from 'react-bootstrap';
// import '../styles/Pagination.css'; 

////////////////////////////// DELETE THIS LATER///////////////////////////

function generateMockProducts(count) {
  const products = [];
  for (let i = 0; i < count; i++) {
    const product = {
      id: i + 1,
      model: `Example-Model-${i+1}`,
      deviceType: "Example-Name",
      serialNumber: "Example-Id-123456789",
    };
    products.push(product);
  }
  return products;
}

const fakeProducts = generateMockProducts(1000);

//////////////////////////////////////////////////////////////////////

export function ProductsTab(props) {
  const productsPage = useContext(UserContext).products;
  //var products = fakeProducts// productsPage;
  var products = productsPage;
  if(products==null) products=[]
  const [show, setShow] = useState(false);
  const maxColumns = 4;
  const productsPerPage = maxColumns*4;
  const totalPages = Math.ceil(products.length / productsPerPage);
  const [currentPage, setCurrentPage] = useState(0);

  const handlePageChange = (selectedPage) => {
    setCurrentPage(selectedPage);
  };

  const renderPaginationItems = () => {
    const items = [];

    // Calcula los límites de los elementos a mostrar
    let startPage = Math.max(currentPage - 2, 0);
    let endPage = Math.min(startPage + 4, totalPages - 1);
    startPage = Math.max(endPage - 4, 0);

    for (let page = startPage; page <= endPage; page++) {
      items.push(
        <Pagination.Item
          key={page}
          active={page === currentPage}
          onClick={() => handlePageChange(page)}
        >
          {page + 1}
        </Pagination.Item>
      );
    }

    return items;
  };

  const offset = currentPage * productsPerPage;
  const currentPageProducts = products.slice(offset, offset + productsPerPage);

  return (
    <>
      <Button onClick={() => setShow(true)}>Register a new product</Button>
      <RegisterNewProductModal
        show={show}
        handleClose={() => setShow(false)}
        handleCreate={() => {
          console.log('CREATE');
        }}
      />
      <CardGroup>
        {products === undefined || products.length === 0 ? (
          <EmptySearch />
        ) : (
          <Row xs={1} md={2} lg={3} xl={maxColumns}>
            {currentPageProducts.map((product) => (
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
            disabled={currentPage === 0}
            onClick={() => handlePageChange(0)}
          />
          <Pagination.Prev
            disabled={currentPage === 0}
            onClick={() => handlePageChange(currentPage - 1)}
          />
          {renderPaginationItems()}
          <Pagination.Next
            disabled={currentPage === totalPages - 1 || products.length == 0}
            onClick={() => handlePageChange(currentPage + 1)}
          />
          <Pagination.Last
            disabled={currentPage === totalPages - 1 || products.length == 0}
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
    <Card key={props.product} className="productCard">
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
    const product = { serialNumber }
    props.handleCreate(product);
    props.handleClose();
  }

  return <Modal show={props.show} onHide={props.handleClose} className="custom-modal">
    <Modal.Header closeButton className="custom-modal-header">
      <Modal.Title>Register a new product</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <Form>
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
