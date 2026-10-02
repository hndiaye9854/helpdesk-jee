<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="isNew" value="${empty user.id}" />
<c:set var="pageTitle" value="${isNew ? 'Nouvel utilisateur' : 'Modifier l\\'utilisateur'}" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<h1 class="h3 mb-3"><c:out value="${pageTitle}" /></h1>

<c:if test="${not empty globalError}">
    <div class="alert alert-danger"><c:out value="${globalError}" /></div>
</c:if>

<%-- novalidate : la validation de référence est côté serveur --%>
<form method="post" action="${ctx}/users/save" class="card card-body" style="max-width: 640px" novalidate>
    <input type="hidden" name="_csrf" value="${csrfToken}">
    <c:if test="${not isNew}">
        <input type="hidden" name="id" value="${user.id}">
        <input type="hidden" name="version" value="${user.version}">
    </c:if>

    <div class="row">
        <div class="col-md-6 mb-3">
            <label for="firstName" class="form-label">Prénom</label>
            <input type="text" id="firstName" name="firstName" maxlength="50"
                   class="form-control ${not empty errors.firstName ? 'is-invalid' : ''}"
                   value="${fn:escapeXml(user.firstName)}">
            <div class="invalid-feedback"><c:out value="${errors.firstName}" /></div>
        </div>
        <div class="col-md-6 mb-3">
            <label for="lastName" class="form-label">Nom</label>
            <input type="text" id="lastName" name="lastName" maxlength="50"
                   class="form-control ${not empty errors.lastName ? 'is-invalid' : ''}"
                   value="${fn:escapeXml(user.lastName)}">
            <div class="invalid-feedback"><c:out value="${errors.lastName}" /></div>
        </div>
    </div>

    <div class="mb-3">
        <label for="email" class="form-label">Email</label>
        <input type="email" id="email" name="email" maxlength="120"
               class="form-control ${not empty errors.email ? 'is-invalid' : ''}"
               value="${fn:escapeXml(user.email)}">
        <div class="invalid-feedback"><c:out value="${errors.email}" /></div>
    </div>

    <div class="mb-4">
        <label for="role" class="form-label">Rôle</label>
        <select id="role" name="role" class="form-select ${not empty errors.role ? 'is-invalid' : ''}">
            <c:forEach var="r" items="${roles}">
                <option value="${r}" ${r == user.role ? 'selected' : ''}><c:out value="${r.label}" /></option>
            </c:forEach>
        </select>
        <div class="invalid-feedback"><c:out value="${errors.role}" /></div>
    </div>

    <div class="d-flex gap-2">
        <button type="submit" class="btn btn-primary">Enregistrer</button>
        <a href="${ctx}/users" class="btn btn-outline-secondary">Annuler</a>
    </div>
</form>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>