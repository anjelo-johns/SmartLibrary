<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><c:out value="${pageTitle}" default="Smart Library"/> — Stackline</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Source+Serif+4:ital,wght@0,500;0,600;0,700;1,500&amp;family=Inter:wght@400;500;600;700&amp;display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="app-shell">
  <aside class="side">
    <div class="brand">Stackline</div>
    <div class="brand-sub">SMART LIBRARY MANAGEMENT</div>
    <c:set var="cp" value="${pageContext.request.contextPath}"/>
    <c:choose>
      <c:when test="${sessionScope.user.role == 'STUDENT'}">
        <nav class="side-nav">
          <a href="${cp}/student/dashboard" class="${activeNav=='dashboard' ? 'active' : ''}">Dashboard</a>
          <a href="${cp}/student/catalog" class="${activeNav=='catalog' ? 'active' : ''}">Catalog</a>
          <a href="${cp}/student/my-books" class="${activeNav=='mybooks' ? 'active' : ''}">My books</a>
          <a href="${cp}/student/reservations" class="${activeNav=='reservations' ? 'active' : ''}">Reservations</a>
          <a href="${cp}/student/fines" class="${activeNav=='fines' ? 'active' : ''}">Fines</a>
          <a href="${cp}/student/history" class="${activeNav=='history' ? 'active' : ''}">History</a>
          <a href="${cp}/student/profile" class="${activeNav=='profile' ? 'active' : ''}">Profile</a>
        </nav>
      </c:when>
      <c:when test="${sessionScope.user.role == 'LIBRARIAN'}">
        <nav class="side-nav">
          <a href="${cp}/librarian/dashboard" class="${activeNav=='dashboard' ? 'active' : ''}">Dashboard</a>
          <a href="${cp}/librarian/circulation" class="${activeNav=='circulation' ? 'active' : ''}">Circulation desk</a>
          <a href="${cp}/librarian/books" class="${activeNav=='books' ? 'active' : ''}">Manage collection</a>
          <a href="${cp}/librarian/students" class="${activeNav=='students' ? 'active' : ''}">Students</a>
          <a href="${cp}/librarian/reservations" class="${activeNav=='reservations' ? 'active' : ''}">Reservations</a>
          <a href="${cp}/librarian/fines" class="${activeNav=='fines' ? 'active' : ''}">Fines &amp; overdue</a>
          <a href="${cp}/librarian/reports" class="${activeNav=='reports' ? 'active' : ''}">Reports</a>
          <a href="${cp}/librarian/profile" class="${activeNav=='profile' ? 'active' : ''}">Profile</a>
        </nav>
      </c:when>
    </c:choose>
    <div class="side-foot">
      <div class="who"><c:out value="${sessionScope.user.fullName}"/></div>
      <div class="who-role">
        <c:choose>
          <c:when test="${sessionScope.user.role == 'STUDENT'}">Student &middot; <c:out value="${sessionScope.user.registerNumber}"/></c:when>
          <c:otherwise>Library staff</c:otherwise>
        </c:choose>
      </div>
      <a class="logout-btn" href="${cp}/logout">Sign out</a>
    </div>
  </aside>
  <main class="content">
    <c:if test="${not empty sessionScope.flashSuccess}">
      <div class="alert alert-success" data-autohide><c:out value="${sessionScope.flashSuccess}"/></div>
      <c:remove var="flashSuccess" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashError}">
      <div class="alert alert-error" data-autohide><c:out value="${sessionScope.flashError}"/></div>
      <c:remove var="flashError" scope="session"/>
    </c:if>
