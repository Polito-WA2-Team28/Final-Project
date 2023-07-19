import { Routes, Route, Navigate, BrowserRouter } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { ToastContainer } from 'react-toastify';
import LandingPage from './pages/landingPage';
import RegisterPage from './pages/registerPage';
import LoginPage from './pages/loginPage';
import Dashboard from './pages/dashboard';
import AppNavBar from './components/AppNavBar';
import authAPI from './APIs/authAPI'
import customerAPI from './APIs/customerAPI';
import expertAPI from './APIs/expertAPI';
import managerAPI from './APIs/managerAPI';
import UserPage from './pages/userPage';
import jwt_decode from "jwt-decode";
import TicketPage from './pages/ticketPage';
import ProductPage from './pages/productPage';
import Roles from './model/rolesEnum';
import { successToast, errorToast } from './components/toastHandler';
import EditUserPage from './pages/editUserPage';
import { ActionContext, UserContext } from './Context';
import TicketState from './model/ticketState';

function App() {

  const [user, setUser] = useState(null);
  const [loggedIn, setLoggedIn] = useState(false);
  const [token, setToken] = useState(null);
  const [tickets, setTickets] = useState([]);
  const [products, setProducts] = useState([]);
  const [role, setRole] = useState(null)
  const [experts, setExperts] = useState(null)
  const [dirty, setDirty] = useState(false)
  const [username, setUsername] = useState(null)


  const handleLogin = async (credentials) => {
    await authAPI.login(credentials)
      .then((data) => {
        var decoded = jwt_decode(data);
        const newRole = decoded.resource_access["ticketing-service-client"].roles[0]
        setUsername(decoded.preferred_username)
        setRole(newRole)
        setToken(data);
        setLoggedIn(true);
        localStorage.setItem("token", data);
        successToast("Logged in successfully")
      })
  };

  const handleRegistration = async (credentials) => {
    await authAPI.register(credentials)
      .then((data) => {
        setUser(data);
      })
  };

  const handleLogout = async () => {
    setToken(null);
    setLoggedIn(false);
    setUser(null);
    setRole(null);
    setUsername(null);
    setProducts([]);
    setTickets([]);
    setExperts([]);
    localStorage.removeItem("token");
    successToast("Logged out successfully")
  };

  const getTicketByID = async (ticketId) => {
    switch (role) {
      case Roles.CUSTOMER:
        return customerAPI.getTicket(token, ticketId)
          .catch((err) => errorToast(err));
      case Roles.EXPERT:
        return expertAPI.getTicket(token, ticketId)
          .catch((err) => errorToast(err));
      case Roles.MANAGER:
        return managerAPI.getTicket(token, ticketId)
          .catch((err) => errorToast(err));
      default:
        console.error("Error: No role found")
        break;
    }
  }

  const getProductByID = async (productId) => {
    switch (role) {
      case Roles.CUSTOMER:
        return await customerAPI.getProduct(token, productId)
          .then((product) => { return product })
          .catch((err) => errorToast(err));
      case Roles.EXPERT:
        return await expertAPI.getProduct(token, productId)
          .then((product) => { return product })
          .catch((err) => errorToast(err));
      case Roles.MANAGER:
        return await managerAPI.getProduct(token, productId)
          .then((product) => { return product })
          .catch((err) => errorToast(err));
      default:
        console.error("Error: No role found")
        break;
    }
  }

  const handleCreateTicket = async (ticket) => {
    await customerAPI.createTicket(token, ticket)
      .then(() => {
        setDirty(true)
      })
  };

  async function customerGetTickets(noPage) {
    await customerAPI.getTicketsPage(token, noPage)
      .then(tickets => {
        setTickets(tickets)
      })
      .catch((err) => errorToast(err));
  }

  const customerGetProducts = async (noPage) => {
    await customerAPI.getProductsPage(token, noPage)
      .then(products => {
        setProducts(products);
      })
      .catch((err) => errorToast(err));
  }

  async function expertGetTickets(noPage) {
    await expertAPI.getTicketsPage(token, noPage)
      .then(tickets => { setTickets(tickets) })
      .catch((err) => errorToast(err));
  }
  async function managerGetTickets(noPage, filter) {
    await managerAPI.getTicketsPage(token, noPage, filter)
      .then(tickets => { setTickets(tickets) })
      .catch((err) => errorToast(err));
  }
  async function managerGetProducts(noPage) {
    await managerAPI.getProductsPage(token, noPage)
      .then(products => { console.log(products); setProducts(products) })
      .catch((err) => errorToast(err));
  }
  async function managerGetExperts(noPage) {
    await managerAPI.getExpertsPage(token, noPage)
      .then(experts => { setExperts(experts) })
      .catch((err) => errorToast(err));
  }

  const getTicketPage = async (newPageNo, filter) => {
    switch (role) {
      case Roles.CUSTOMER:
        customerGetTickets(newPageNo);
        break;
      case Roles.EXPERT:
        expertGetTickets(newPageNo);
        break;
      case Roles.MANAGER:
        managerGetTickets(newPageNo, filter);
        break;
      default:
        console.error("Error: No role found")
    }
  }

  const handleEditProfile = async (profile) => {
    await customerAPI.patchProfile(token, profile)
      .then(() => {
        setUser(profile)
        successToast("Changes saved!")
      })
      .catch((err) => {
        errorToast(err)
      });

  };

  useEffect(() => {
    const checkAut = async () => {
      await customerAPI.getProfile(token)
        .then((user) => setUser(() => user))
        .catch((err) => {
          if (err !== "Not authenticated") {
            errorToast(err)
          }
        });
    }
    if (role === Roles.CUSTOMER)
      checkAut();
  }, [token, role, dirty])

  useEffect(() => {

    switch (role) {
      case Roles.CUSTOMER:
        customerGetTickets(1); customerGetProducts(1);
        break;
      case Roles.EXPERT:
        expertGetTickets(1);
        break;
      case Roles.MANAGER:
        managerGetTickets(1); managerGetProducts(1); managerGetExperts(1);
        break;
      default:
        break;
    }
    setDirty(false)

  }, [loggedIn, token, role, dirty])

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      var decoded = jwt_decode(token);
      const newRole = decoded.resource_access["ticketing-service-client"].roles[0]
      if (decoded.exp > (Date.now() / 1000)) {
        setRole(newRole)
        setToken(token);
        setLoggedIn(true);
      }
    }
  }, []);

  const customerCompileSurvey = async (ticketId, survey) => {
    await customerAPI.compileSurvey(token, ticketId, survey)
      .then(() => {
        setTickets((prev) => prev.filter((ticket) => ticket.ticketId !== ticketId))
        successToast("Survey submitted!")
        setDirty(true)
      })
      .catch((err) => errorToast(err))
  }

  const customerReopenTicket = async (ticketId) => {
    await customerAPI.reopenTicket(token, ticketId)
      .then(() => { successToast("Ticket reopened"); setDirty(true) })
      .catch(err => errorToast(err))
  }

  const getMessages = async (ticketId, pageNo) => {

    switch (role) {
      case Roles.CUSTOMER:
        return await customerAPI.getMessagesPage(token, ticketId, pageNo)
          .catch((err) => errorToast(err));
      case Roles.EXPERT:
        return await expertAPI.getMessagesPage(token, ticketId, pageNo)
          .catch((err) => errorToast(err));
      case Roles.MANAGER:
        return await managerAPI.getMessagesPage(token, ticketId, pageNo)
          .catch((err) => errorToast(err));
      default:
        errorToast("You are not allowed to see messages")
    }
  }

  const sendMessage = async (ticketId, message, files) => {
    switch (role) {
      case Roles.CUSTOMER:
        await customerAPI.sendMessage(token, message, ticketId, files)
          .then(() => setDirty(true))
          .catch((err) => errorToast(err));
        break;
      case Roles.EXPERT:
        await expertAPI.sendMessage(token, message, ticketId, files)
          .then(() => setDirty(true))
          .catch((err) => errorToast(err));
        break;
      default:
        errorToast("You are not allowed to send messages")
        break;
    }
  }

  const managerAssignExpert = async (ticketId, expertId) => {
    await managerAPI.assignTicket(token, ticketId, expertId)
      .then(() => { successToast("Expert assigned"); setDirty(true) })
      .catch((err) => errorToast(err));
  }

  const managerHandleCloseTicket = async (ticket) => {
    switch (ticket.ticketState) {
      case TicketState.OPEN:
        managerAPI.closeTicket(token, ticket.ticketId)
          .then(() => { successToast("ticket closed"); setDirty(true) })
        break
      case TicketState.IN_PROGRESS:
        managerAPI.closeTicket(token, ticket.ticketId)
          .then(() => { successToast("ticket closed"); setDirty(true) })
        break
      case TicketState.REOPENED:
        managerAPI.closeTicket(token, ticket.ticketId)
          .then(() => { successToast("ticket closed"); setDirty(true) })
        break
      default:
        console.error('Invalid ticket state')
        throw new Error('Invalid ticket state')
    }
  }

  const managerRelieveExpert = async (ticketId) => {
    await managerAPI.relieveExpert(token, ticketId)
      .then(() => { setDirty(true); successToast("Expert relieved!") })
      .catch((err) => errorToast(err));
  }

  const expertResolveTicket = async (ticketId) => {
    await expertAPI.resolveTicket(token, ticketId)
      .then(() => { setDirty(true); successToast("Ticket resolved!") })
      .catch((err) => errorToast(err));
  }

  const getAttachment = async (ticketId, attachmentName) => {
    switch (role) {
      case Roles.CUSTOMER:
        return await customerAPI.getAttachment(token, ticketId, attachmentName)
          .catch((err) => errorToast(err));
      case Roles.EXPERT:
        return await expertAPI.getAttachment(token, ticketId, attachmentName)
          .catch((err) => errorToast(err));
      case Roles.MANAGER:
        return await managerAPI.getAttachment(token, ticketId, attachmentName)
          .catch((err) => errorToast(err));
      default:
        errorToast("You are not allowed to see messages")
    }
  }

  const registerProduct = async (product) => {
    await customerAPI.registerProduct(token, product)
      .then(() => { customerGetProducts(1); successToast("Product registered!") })

  }

  const getExpertsPage = async (pageNo) => {
    await managerAPI.getExpertsPage(token, pageNo)
      .then((expertsPage) => setExperts(expertsPage))
      .catch((err) => errorToast(err));
  }

  const registerExpert = async (expert) => {

    console.log("Registering expert: " + expert)

    await managerAPI.registerExpert(token, expert)
      .then(() => { managerGetExperts(1); successToast("Expert registered!") })
      .catch((err) => errorToast(err));
  }


  const actions = {
    getMessages, sendMessage, handleLogin, handleLogout, registerExpert,
    handleRegistration, handleEditProfile, handleCreateTicket, getTicketByID,
    getProductByID, customerCompileSurvey, customerReopenTicket, managerAssignExpert,
    managerHandleCloseTicket, managerRelieveExpert, expertResolveTicket, getAttachment,
    registerProduct, getTicketPage, getExpertsPage,
  }

  const userValues = {
    user, loggedIn, role,
    products, tickets, experts, username,
  }

  return (
    <ActionContext.Provider value={actions}>
      <UserContext.Provider value={userValues}>
        <BrowserRouter>
          <AppNavBar />
          <Routes>
            <Route path="/" element={loggedIn ? <Navigate to={"/dashboard"} /> : <LandingPage />} />
            <Route path="/register" element={loggedIn ? <Navigate to={"/dashboard"} /> : <RegisterPage />} />
            <Route path="/login" element={loggedIn ? <Navigate to={"/dashboard"} /> : <LoginPage />} />
            <Route path="/dashboard" element={loggedIn ? <Dashboard /> : <Navigate to={"/"} />} />
            <Route path="/user" element={loggedIn ? <UserPage user={user} /> : <Navigate to={"/"} />} />
            <Route path="/editUser" element={loggedIn ? <EditUserPage user={user} handleEdit={handleEditProfile} /> : <Navigate to={"/"} />} />
            <Route path="/ticket/:ticketId" element={loggedIn ? <TicketPage /> : <Navigate to={"/"} />} />
            <Route path="/product/:productId" element={loggedIn ? <ProductPage /> : <Navigate to={"/"} />} />

            <Route path="*" element={<h1 >Not Found</h1>} />
          </Routes>
        </BrowserRouter>
        <ToastContainer />
      </UserContext.Provider>
    </ActionContext.Provider>
  )
}

export default App;
