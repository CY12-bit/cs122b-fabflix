function createTable({table_name, columns}) {
    let rows = columns.map(({column_name, data_type}) => {
        return `
            <tr>
                <td>${column_name}</td>
                <td>${data_type}</td>
            </tr>`;
    });
    return `
        <h3 class="table-name">${table_name}</h3>
        <table class="table table-striped">
            <thead>
            <tr>
                <th>Column Name</th>
                <th>Type</th>
            </tr>
            </thead>

            <tbody id="movie_table_body">${rows.join("")}</tbody>
        </table>
    `;
}

function populateDashboard(tableData) {
    console.log('populate dashboard', tableData)
    let tableDiv = $("#table-data");
    tableData.forEach(table => {
        tableDiv.append(createTable(table))
    })
}

$.ajax(
    "../api/employee/dashboard", {
        method: "GET",
        success: populateDashboard
    }
);