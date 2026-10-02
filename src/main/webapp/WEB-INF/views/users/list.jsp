<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="pageTitle" value="Utilisateurs" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 mb-0">Utilisateurs</h1>
    <a class="btn btn-primary" href="${ctx}/users/new">Nouvel utilisateur</a>
</div>

<c:choose>
    <c:when test="${empty users}">
        <p class="text-muted">Aucun utilisateur pour le moment.</p>
    </c:when>
    <c:otherwise>
        <table class="table table-hover align-middle">
            <thead>
            <tr>
                <th>Nom</th>
                <th>Email</th>
                <th>Rôle</th>
                <th>Créé le</th>
                <th class="text-end">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="u" items="${users}">
                <tr>
                    <td><c:out value="${u.fullName}" /></td>
                    <td><c:out value="${u.email}" /></td>
                    <td>
                        <span class="badge ${u.technician ? 'text-bg-info' : 'text-bg-secondary'}">
                            <c:out value="${u.role.label}" />
                        </span>
                    </td>
                    <td>${hd:dateTime(u.createdAt)}</td>
                    <td class="text-end">
                        <a class="btn btn-sm btn-outline-secondary" href="${ctx}/users/edit?id=${u.id}">Modifier</a>
                        <form method="post" action="${ctx}/users/delete" class="d-inline"
                              onsubmit="return confirm('Supprimer cet utilisateur ?');">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="id" value="${u.id}">
                            <button type="submit" class="btn btn-sm btn-outline-danger">Supprimer</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>