var RestControllerModule = (function () {

    /* PRIVATE */
    
    var DEFAULT_ADDRESS = 'http://localhost:8080'; // XXX
    var server_url = DEFAULT_ADDRESS + '/orders';
    
    var getProductPrices = function () {
	return axios.get(server_url + '/products');
    };

    var getOrdersFromServer = function () {
	return axios.get(server_url);
    };

    /**
     * Construct an order in the javascript format
     */
    var constructOrder = function (order, products) {
	var ord = {order_id:null, table_id:null, products:[]};
	ord.order_id = order.tableNumber;
	ord.table_id = order.tableNumber;

	for(p in order.orderAmountsMap) {
	    var prod = {product: p, quantity: order.orderAmountsMap[p], price:null};
	    prod.price = '$' + products.filter(function (e) {
		return e.name === p;
	    })[0].price;

	    ord.products.push(prod);
	}

	return ord;
    };

    var constructServerOrder = function (order) {
	console.log(order);
	var ord = {orderAmountsMap: {}, tableNumber: null};

	ord.tableNumber = order.table_id;
	for(i in order.products) {
	    console.log(order.products[i]);
	    var name = order.products[i].product;
	    var quantity = order.products[i].quantity;

	    ord.orderAmountsMap[name] = quantity;
	}

	return ord;
    };

    var constructOrders = function (orders, products) {
	var res = [];
	
	for(i in orders) {
	    ord = constructOrder(orders[i], products);
	    res.push(ord);
	}
	
	return res;
    };

    /* PUBLIC */

    /**
     * Gets the data from the server and calls the callback 
     * methods onSuccess and onFailed respectively.
     */
    var getOrders = function (callback) {
	axios.all([getProductPrices(), getOrdersFromServer()])
	    .then(axios.spread( function (prices, orders) {
		// console.log(products);
		// console.log(orders.data);
		products = prices.data;
		var ords = constructOrders(orders.data, products);
		callback.onSuccess(ords);
	    }))
	    .catch(function (error) {
		console.log('ERROR: failed to get server data');
		callback.onFailed(error);
	    });
    };

    var updateOrder = function (order, callback) {
	var serverOrder = constructServerOrder(order);
	axios.put(server_url, serverOrder)
	    .then(callback.onSuccess)
	    .catch(function (error) {
		console.log('ERROR: failed to fetch data from server');
		callback.onFailed(error);
	    });
    };

    var deleteOrder = function (orderId, callback) {
	axios.delete(server_url + '/' + orderId)
	    .then(callback.onSuccess)
	    .catch(function (error) {
		console.log('ERROR: failed to send data to server');
		callback.onFailed(error);
	    });
    };

    var createOrder = function (order, callback) {
	// todo implement
    };

    return {
	getOrders: getOrders,
	updateOrder: updateOrder,
	deleteOrder: deleteOrder,
	createOrder: createOrder
    };

})();
