import { Container, Row } from "react-bootstrap";

export default function EmptySearch() {
    return (
        <Container className="mt-5">
            <Row className="d-flex justify-content-center text-center" style={{padding: "20px"}}>
                Sorry there are no results
            </Row>
        </Container>
    );
}
