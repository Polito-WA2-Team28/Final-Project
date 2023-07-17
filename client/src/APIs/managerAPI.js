import { authHeader, compositeHeader } from "./util.js";
const url = "http://localhost:3000/api/managers";

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function getTicketsPage(token, noPages) {
    const res = await fetch(url + `/tickets?pageNo=${noPages}`,
        { method: "GET", headers: authHeader(token) })
    if (!res.ok) throw res.statusText
    const data = await res.json();
    return data;
}


/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function getTicket(token, ticketId) {
    const res = await fetch(url + "/tickets/" + ticketId,
        { method: "GET", headers: authHeader(token) })
    if (!res.ok) throw res.statusText
    const data = await res.json();
    return data;
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function assignTicket(token, ticketId, expertId) {
    const res = await fetch(url + "/tickets/" + ticketId + "/assign",
        { method: "PATCH", headers: compositeHeader(token), body: JSON.stringify({expertId: expertId}) })
    if (!res.ok) throw res.statusText
}


/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function relieveExpert(token, ticketId) {
    const res = await fetch(url + "/tickets/" + ticketId + "/relieveExpert",
        { method: "PATCH", headers: authHeader(token) })
    if (!res.ok) throw res.statusText
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function closeTicket(token, ticketId) {
    const res = await fetch(url + "/tickets/" + ticketId + "/close",
        { method: "PATCH", headers: authHeader(token) })
    if (!res.ok) throw res.statusText
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function resumeProgress(token, ticketId, ticketUpdateData) {
    const res = await fetch(url + "/tickets/" + ticketId + "/resumeProgress",
        { method: "PATCH", headers: authHeader(token) })
    if (!res.ok) throw res.statusText
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function removeTicket(token, ticketId) {
    const res = await fetch(url + "/tickets/" + ticketId + "/remove",
        { method: "DELETE", headers: authHeader(token) })
    if (!res.ok) throw res.statusText
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function sendMessage(token, message, ticketId) {
    const res = await fetch(url + "/tickets/" + ticketId + "/messages",
        { method: "POST", headers: compositeHeader(token), body: JSON.stringify(message) })
    if (!res.ok) throw res.statusText
    const data = await res.json();
    return data;
}


/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function getMessages(token, ticketId) {
    const res = await fetch(url + "/tickets/" + ticketId + "/messages",
        { method: "GET", headers: authHeader(token) })
    if (!res.ok) throw res.statusText
    const data = await res.json();
    return data;
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function getProductsPage(token, noPages) {
    const res = await fetch(url + `/products?pageNo=${noPages}`,
        { method: "GET", headers: authHeader(token) })
    if (!res.ok) throw res.statusText
    const data = await res.json();
    return data;
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function getProduct(token, productId) {
    const res = await fetch(url + "/products/" + productId,
        { method: "GET", headers: authHeader(token) })
    if (!res.ok) throw res.statusText
    const data = await res.json();
    return data;
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function getExpertsPage(token, noPages) {
    const res = await fetch(url + `/experts?pageNo=${noPages}`,
        { method: "GET", headers: authHeader(token) })
    if (!res.ok) throw res.statusText
    const data = await res.json();
    return data;
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function getAttachment(token, ticketId, attachmentName) {
    const res = await fetch(url + "/tickets/" + ticketId + "/attachments/" + attachmentName,
        { method: "GET", headers: authHeader(token) })
    if (!res.ok) throw res.statusText
    const data = await res.json();
    console.log(data);
    return data;
}

 const managerAPI = {
    getTicketsPage, getTicket, assignTicket,
    relieveExpert, closeTicket, resumeProgress, removeTicket,
    sendMessage, getMessages, getProductsPage, getProduct,getExpertsPage, getAttachment
};

export default managerAPI;
