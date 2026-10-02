<%@ page isErrorPage="true" %>
<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="status" value="${requestScope['jakarta.servlet.error.status_code']}" />
<c:set var="errorMessage" value="${requestScope['jakarta.servlet.error.message']}" />
<c:set var="pageTitle" value="Erreur ${status}" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="text-center py-5">
    <h1 class="display-4 fw-bold"><c:out value="${status}" /></h1>
    <c:choose>
        <c:when test="${status == 404}"><p class="lead">Page ou ressource introuvable.</p></c:when>
        <c:when test="${status == 403}"><p class="lead">Action refusée.</p></c:when>
        <c:when test="${status == 409}"><p class="lead">Opération impossible.</p></c:when>
        <c:when test="${status >= 500}"><p class="lead">Une erreur inattendue est survenue.</p></c:when>
        <c:otherwise><p class="lead">Requête invalide.</p></c:otherwise>
    </c:choose>

    <%-- Détail uniquement pour les erreurs "métier" : jamais de détail technique sur une 500 --%>
    <c:if test="${status < 500 and not empty errorMessage}">
        <p class="text-muted"><c:out value="${errorMessage}" /></p>
    </c:if>

    <a class="btn btn-primary mt-3" href="${ctx}/">Retour à l'accueil</a>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>