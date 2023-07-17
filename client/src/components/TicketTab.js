import { faUpRightAndDownLeftFromCenter } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useContext, useState } from "react";
import { Button, Card, CardGroup, Col, Row} from "react-bootstrap";
import EmptySearch from "./EmptySearch";
import { useNavigate } from "react-router-dom";
// import "../styles/TicketTab.css"
import { UserContext } from "../Context";
import { Pagination } from 'react-bootstrap';

export default function TicketTab(prop) {

  const ticketsPage = useContext(UserContext).tickets
  var tickets = ticketsPage.content
  // const tickets = fakeTickets

  if(tickets==null) tickets=[]

  var ticketsPerPage = ticketsPage.pageSize;
  var totalPages = ticketsPage.totalPages;
  var [currentPage, setCurrentPage] = useState(0);

  const handlePageChange = (page) => {
    prop.newTicketPage(page)
    setCurrentPage(page);
  };

  const renderPaginationItems = () => {
    const items = [];
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

  return (
    <>
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
            disabled={currentPage === 0}
            onClick={() => handlePageChange(0)}
          />
          <Pagination.Prev
            disabled={currentPage === 0}
            onClick={() => handlePageChange(currentPage - 1)}
          />
          {renderPaginationItems()}
          <Pagination.Next
            disabled={currentPage === totalPages - 1 || tickets.length == 0}
            onClick={() => handlePageChange(currentPage + 1)}
          />
          <Pagination.Last
            disabled={currentPage === totalPages - 1 || tickets.length == 0}
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
            <p className="my-0">{props.ticket.description}</p>
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
