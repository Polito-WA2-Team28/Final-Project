import { faUpRightAndDownLeftFromCenter } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useContext, useState } from "react";
import { Button, Card, CardGroup, Col, Row} from "react-bootstrap";
import EmptySearch from "./EmptySearch";
import { useNavigate } from "react-router-dom";
import "../styles/TicketTab.css"
import { UserContext } from "../Context";
import { Pagination } from 'react-bootstrap';

/// REMOVE ////////////////////////////////////////////////

function generateMockTickets(count) {
  const tickets = [];
  for (let i = 0; i < count; i++) {
    const ticket = {
      ticketId: i + 1,
      title: `Example Title ${i + 1}`,
      description: `Lorem ipsum dolor sit amet, consectetur adipiscing elit. Lorem ipsum dolor sit amet, consectetur adipiscing elit. `,
    };
    tickets.push(ticket);
  }
  return tickets;
}

const fakeTickets = generateMockTickets(1000);

/// REMOVE /////////////////////////////////////


export default function TicketTab() {

  const ticketsPage = useContext(UserContext).tickets
  var tickets = ticketsPage.content
  // const tickets = fakeTickets

  if(tickets==null) tickets=[]

  const ticketsPerPage = 10;
  const totalPages = Math.ceil(tickets.length / ticketsPerPage);
  const [currentPage, setCurrentPage] = useState(0);

  const handlePageChange = (page) => {
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

  const offset = currentPage * ticketsPerPage;
  const currentTickets = tickets.slice(offset, offset + ticketsPerPage);

  return (
    <>
      <CardGroup>
        {(tickets === undefined || tickets.length === 0) ? (
          <EmptySearch />
        ) : (
          <Row xs={1} md={2} lg={3} xl={4}>
            {currentTickets.map((ticket) => (
              <Col key={ticket.ticketId}>
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
    <Card className="ticketCard h-100">
      <Card.Body>
        <Card.Title>
          <Row>
            <Col>{props.ticket.title}</Col>
            <Col className="text-end">
              <Button onClick={() => navigate(`/ticket/${props.ticket.ticketId}`)}>
                <FontAwesomeIcon icon={faUpRightAndDownLeftFromCenter} />
              </Button>
            </Col>
          </Row>
        </Card.Title>
        <p>{props.ticket.description}</p>
      </Card.Body>
    </Card>
  );
}
