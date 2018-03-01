function addOrder(order) {
    var tables = document.getElementById("order_tables");

    var table = document.createElement("table");
    table.setAttribute("id", "table" + order.table_id);
    table.setAttribute("class", "orders_table");

    var tbody = document.createElement("tbody");

    var tableHeader = document.createElement("tr");
    tableHeader.setAttribute("class", "table_header");
    
    var headerRow = document.createElement("th");
    headerRow.innerHTML = "Product";
    tableHeader.appendChild(headerRow);
    headerRow = document.createElement("th");
    headerRow.innerHTML = "Quantity";
    tableHeader.appendChild(headerRow);
    headerRow = document.createElement("th");
    headerRow.innerHTML = "Price";
    tableHeader.appendChild(headerRow);
    tbody.appendChild(tableHeader);

    for(index = 0 ; index < order.products.length; index++) {
	var product = order.products[index];
	var row = document.createElement("tr");
	row.setAttribute("id", "table" + order.table_id + "_dish" + (index+1));
	row.setAttribute("class", "table_product");
	tbody.appendChild(row);
	
	var productName = document.createElement("td");
	productName.setAttribute("class", "product_name");
	productName.innerHTML = product.product;
	
	var productQuantity = document.createElement("td");
	productQuantity.setAttribute("class", "product_quantity");
	productQuantity.innerHTML = product.quantity;
	
	var productPrice = document.createElement("td");
	productPrice.setAttribute("class", "product_price");
	productPrice.innerHTML = product.price;
	
	row.appendChild(productName);
	row.appendChild(productQuantity);
	row.appendChild(productPrice);
    }

    table.appendChild(tbody);
    tables.appendChild(table);
}

/**
 * Removes order by it's id
 */
function removeOrderById(id) {
    var table = document.getElementById("table" + id);
    if (table != null) {
	table.remove();
    }
}

var PRODUCTS;

function getProductPrices() {
    return axios.get(DEFAULT_ADDRESS + '/orders/products');
}

function getOrders() {
    return axios.get(DEFAULT_ADDRESS + '/orders');
}

/**
 * Loads orders from the server
 */
function loadOrders() {
    clearTables();
    getData(addOrdersFromAPI);
}

/**
 * Clears all the page's tables
 */
function clearTables() {
    document.getElementById('order_tables').innerHTML = '';
}

/**
 * // TODO por definir
 */
function getTableOrders(tableid) {
    // TODO
}

/**
 * Adds all orders from server's API
 */
function addOrdersFromAPI(orders) {
    for(i in orders) {
	ord = constructOrder(orders[i], PRODUCTS);
	addOrder(ord);
    }
}

function constructOrder(order, products) {
    var ord = {order_id:null, table_id:null, products:[]};
    ord.order_id = order.tableNumber;
    ord.table_id = order.tableNumber;

    for(p in order.orderAmountsMap) {
	var prod = {product: p, quantity: order.orderAmountsMap[p], price:null};
	prod.price = '$' + products.filter(function (e) {
	    return e.name === p;
	})[0].price;

	assert(products.filter(function (e) {
	    return e.name === p;
	}).length == 1);

	ord.products.push(prod);
    }

    return ord;
}

function assert(condition) {
    if (condition === false) {
	console.log('Assertion Error');
    }
}

var DEFAULT_ADDRESS = ''/*'http://localhost:8080'*/; // XXX
var server_url = DEFAULT_ADDRESS + '/orders';

/**
 * Obtains the orders from the main server
 */
function getData(callback_func) {
    axios.all([getProductPrices(), getOrders()])
	.then(axios.spread( function (prices, orders) {
	    PRODUCTS = prices.data;
	    callback_func(orders.data);
	}))
	.catch(function (error) {
	    showErrorMessage();
	});
}

/**
 * Shows an error popup with a message
 */
function showErrorMessage(msg = 'There is a problem with our servers. We apologize for the inconvince, please try again later') {
    console.log('ERROR: ' + msg);
    alert('ERROR: ' + msg);
}
