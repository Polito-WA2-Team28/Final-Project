import { faUpRightAndDownLeftFromCenter } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useContext, useState } from "react";
import { Button, Card, CardGroup, Col, Dropdown, DropdownButton, Row } from "react-bootstrap";
import EmptySearch from "./EmptySearch";
import { useNavigate } from "react-router-dom";
import { ActionContext, UserContext } from "../Context";
import { Pagination } from 'react-bootstrap';
import Roles from "../model/rolesEnum";

export default function TicketTab() {

  const { getTicketPage } = useContext(ActionContext)
  const [filter, setFilter] = useState("ALL")


  const ticketsPage = useContext(UserContext).tickets
  const role = useContext(UserContext).role

  var tickets = ticketsPage.content

  if (tickets == null) tickets = []

  var totalPages = ticketsPage.totalPages;
  var [currentPage, setCurrentPage] = useState(1);

  const handlePageChange = (page, filter) => {
    getTicketPage(page, filter)
    setCurrentPage(page);
  };

  const handleFilterChange = (filter) => {
    setFilter(filter)
    handlePageChange(1, filter)
  }

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
      {role === Roles.MANAGER &&
        <Row style={{ margin: "10px" }}>
          <Col >
            <DropdownButton style={{ margin: "0", padding: "0" }} title="Choose state">
              <Dropdown.Item onClick={() => handleFilterChange("")}>ALL</Dropdown.Item>
              <Dropdown.Item onClick={() => handleFilterChange("OPEN")}>OPEN</Dropdown.Item>
              <Dropdown.Item onClick={() => handleFilterChange("IN_PROGRESS")}>IN PROGRESS</Dropdown.Item>
              <Dropdown.Item onClick={() => handleFilterChange("CLOSED")}>CLOSED</Dropdown.Item>
              <Dropdown.Item onClick={() => handleFilterChange("RESOLVED")}>RESOLVED</Dropdown.Item>
              <Dropdown.Item onClick={() => handleFilterChange("REOPENED")}>REOPENED</Dropdown.Item>
            </DropdownButton>
          </Col>
          <Col className="text-end">
            <h3>Active Filter: {filter}</h3>
            <h3>Num: {tickets.length}</h3>
          </Col>
        </Row>
      }


      <CardGroup className="mt-1">
        {(tickets === undefined || tickets.length === 0) ? (
          <EmptySearch />
        ) : (
          <Row xs={1} className="w-100 justify-content-center">
            {tickets.map((ticket) => (
              <Col key={ticket.ticketId} className="mb-1 w-75">
                <TicketItem ticket={ticket} />
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
            disabled={currentPage === totalPages || tickets.length === 0}
            onClick={() => handlePageChange(currentPage + 1)}
          />
          <Pagination.Last
            disabled={currentPage === totalPages || tickets.length === 0}
            onClick={() => handlePageChange(totalPages - 1)}
          />
        </Pagination>
      </div>
    </>
  );
}

function TicketItem(props) {
  const navigate = useNavigate();

  return (
    <Card className="">
      <Card.Body>
        <Card.Title>
          <Row xs={2} className="align-items-center">
            <p className="my-0">{props.ticket.ticketId} - {props.ticket.description} - {props.ticket.ticketState}</p>
            <Col className="text-end">
              <Button onClick={() => navigate(`/ticket/${props.ticket.ticketId}`)}>
                <FontAwesomeIcon icon={faUpRightAndDownLeftFromCenter} />
              </Button>
            </Col>
          </Row>
        </Card.Title>
      </Card.Body>
    </Card>
  );
}
