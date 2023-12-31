import { Container,  Tab,  Tabs } from "react-bootstrap";
import TicketTab from "../components/TicketTab";
import { ProductsTab } from "../components/ProductsTab";
import Roles from "../model/rolesEnum";
import { useContext } from "react";
import { UserContext } from "../Context";
import ExpertsInfoTab from "../components/expertsInfoTab";

export default function Dashboard(prop) {

  const { role } = useContext(UserContext)

  return (
    <Container>
      <Tabs>
        <Tab eventKey="tickets" title="Tickets">
          <TicketTab/>
        </Tab>
        {(role === Roles.CUSTOMER || role === Roles.MANAGER) &&
          <Tab eventKey="products" title="Products">
            <ProductsTab />
          </Tab>}
        {role === Roles.MANAGER &&
          <Tab eventKey="experts" title="Experts">
            <ExpertsInfoTab />
            </Tab>}
      </Tabs>
    </Container>
  );
}
