var OrdersControllerModule = (function () {

    /* PRIVATE */
    
    var addTable = function (order) {
	var new_table = $('<table>')
	    .attr('id', 'table' + order.tableid)
	    .addClass('orders_table')
	    .append( $('<tr>')
		     .addClass('table_header')
		     .append( $('<th>')
			      .text('Product')
			    ).append( $('<th>')
				      .text('Quantity')
				    ).append( $('<th>')
					      .text('Price')
					    )
		   );

	var products = order.products;
	var tableid = order.table_id;
	for(i in products) {
	    new_table
		.append( $('<tr>')
			 .attr('id', 'table' + tableid + '_dish' + (i+1))
			 .addClass('table_product')
			 .append( $('<td>')
				  .addClass('product_name')
				  .text(products[i].product)
				).append( $('<td>')
					  .addClass('product_quantity')
					  .text(products[i].quantity)
					).append( $('<td>')
						  .addClass('product_price')
						  .text('$' + Number(products[i].price.replace('$', '')) * products[i].quantity)
						)
		       );
	}

	$('#order_tables').append(new_table);
	$('#order_tables').append( $('<input>')
				   .attr('id', 'tableDeleteBtn' + tableid)
				   .attr('type', 'button')
				   .attr('value', 'Delete Table')
				   .click( function () {
				       RestControllerModule.deleteOrder(tableid, {
					   onSuccess: function (dummy) {
					       showOrdersByTable();
					   },
					   onFailed: function (error) {
					       console.log('ERROR: could not delete table ' + tableid);
					       console.log(error);
					   }
				       });
				   })
				 );
	
    };

    var addOrdersUpdateTable = function (orders, tableid) {
	console.log('tableid: ', tableid);
	clearTables();

	assert(orders.length > 0, 'there\'s are no orders available');

	var order = orders.filter(function (e) {
	    return e.table_id == tableid;
	});

	assert(order.length == 1, 'order.length != 1, this should not happen (' + order.length + ')');
	console.log(order[0]);

	changeAdditionTable(orders, tableid);
	addUpdateTable(order[0]);
    };

    var lastTableUpdated = null;

    var changeAdditionTable = function (orders, tableid) {
	assert(tableid != null, 'tableid == null, this should not happen');

	// remove all options (they could have changed)
	$('.table_option').remove();
	
	for(i in orders) {
	    $('#table_selection')
		.append( $('<option>')
			 .addClass('table_option')
			 .attr('value', orders[i].table_id)
			 .text('Table ' + orders[i].table_id)
		       );
	}

	$('.table_option[value=\'' + tableid + '\']').attr('selected', '1');

	$('#item_addBtn').click(function (){
	    var product = $('#item_name').val();
	    var quantity = Number($('#item_quantity').val());

	    var item = {
		product: product,
		quantity: quantity,
		price: null
	    };

	    addItemToOrder(tableid, item);
	    showOrdersOfTable();
	});

	lastTableUpdated = tableid;
    };

    var addUpdateTable = function (order) {
	for(i in order.products) {
	    $('#table_items_update:last-child')
		.append( $('<tr>')
			 .addClass('table_item')
			 .append( $('<td>')
				  .attr('id', 'table_item_name_' + (i+1))
				  .append( $('<input>')
					   .addClass('item_name')
					   .attr('id', 'item_name_' + (i+1))
					   .attr('type', 'text')
					   .attr('placeholder', 'Item Name')
					   .attr('value', order.products[i].product)
					 )
				)
			 .append( $('<td>')
				  .attr('id', 'table_item_quantity_' + (i+1))
				  .append( $('<input>')
					   .addClass('item_quantity')
					   .attr('id', 'item_quantity_' + (i+1))
					   .attr('type', 'number')
					   .attr('min', '1')
					   .attr('value', order.products[i].quantity)
					 )
				)
			 .append( $('<td>')
				  .attr('id', 'table_item_updateBtn' + (i+1))
				  .append( $('<input>')
					   .attr('type', 'button')
					   .attr('value', 'Update')
					   .attr('onclick', 'OrdersControllerModule.updateOrder();')
					 )
				)
			 .append( $('<td>')
				  .attr('id', 'table_item_deleteBtn' + (i+1))
				  .append( $('<input>')
					   .attr('type', 'button')
					   .attr('value', 'Delete')
					   .attr('onclick', 'OrdersControllerModule.deleteOrderItem(\''
						 + order.products[i].product + '\');')
					 )
				)
		       );
	}
    };

    var clearTables = function () {
	// jQuery es una chimba!
	$('#order_tables').html('');

	$('.table_option').remove();
	$('.table_item').remove();
    };

    var getOrders = function (callback) {
	RestControllerModule.getOrders({
	    onSuccess: function(payload){
		currentOrders = payload;
		callback.onSuccess(payload);
	    },
	    onFailed: callback.onFailed
	});
    };

    var assert = function(condition, msg = null) {
	if (!condition) {
	    var assertMsg = 'Assertion Error';
	    if (msg != null) {
		assertMsg += ': ' + msg;
	    }
	    throw new Error(assertMsg);
	}
    };

    var currentOrders = [];

    /* PUBLIC */
    
    var showOrdersByTable = function () {
	var callback = {

            onSuccess: function(ordersList){
		// console.log(ordersList);
		clearTables();
		
		for(var i in ordersList) {
		    addTable(ordersList[i]);
		}
            },
            onFailed: function (exception) {
		console.log(exception);
            }
	};
	
	getOrders(callback);
    };

    var updateOrder = function () {
	tableid = Number($('#table_selection').find(':selected').attr('value'));
	console.log('Table selected is', tableid);

	var order = {
	    order_id: tableid,
	    table_id: tableid,
	    products: []
	};

	$('.table_item').each(function () {
	    var name = $(this).find('.item_name').attr('value');
	    var quantity = $(this).find('.item_quantity').val();
	    // console.log(this, name, quantity);
	    order.products.push({
		product: name,
		quantity: Number(quantity),
		price: null
	    });
	});

	console.log('UPDATE:', order);

	RestControllerModule.updateOrder(order, {
	    onSuccess: function (payload) {
		console.log('Success update!!');
		showOrdersOfTable();
	    },
	    onFailed: function (error) {
		console.log('Failed update :(');
	    }
	});
    };

    var deleteOrderItem = function (itemName) {
	assert(lastTableUpdated != null, 'lastTableUpdated == null, this should not happen');
	assert(currentOrders.length > 0, 'nothing to do, currentOrders is empty');

	var order = currentOrders.filter(function (e){
	    return e.table_id == lastTableUpdated;
	});

	assert(order.length == 1, 'order not found, should not happen');

	var products = order[0].products;
	for(i in products) {
	    if (products[i].product == itemName) {
		delete order[0].products[i];
	    }
	}

	console.log(order[0]);
	RestControllerModule.updateOrder(order[0], {
	    onSuccess: function (payload) {
		console.log('Success deletion!!');
		showOrdersOfTable();
	    },
	    onFailed: function (error) {
		console.log('Failed deletion :(');
	    }
	});
    };

    var addItemToOrder = function (orderId, item) {
	var callback = {
	    onSuccess : function (dummy) {
		console.log(currentOrders);
		var order = currentOrders.filter(function (e) {
		    return orderId == e.table_id;
		});
		console.log(order);
		assert(order.length == 1, "order not found " + order.length);
		order = order[0];

		var prod = order.products.filter(function (e){
		    return e.product == item.product;
		});

		assert(prod.length == 0, 'item already exists ' + prod.length);

		order.products.push(item);
		RestControllerModule.updateOrder(order, {
		    onSuccess: function (payload) {
			showOrdersOfTable();
		    },
		    onFailed: function (error) {
			console.log('ERROR updating the order: ' + error);
		    }});
	    },
	    onFailed: function (error) {
		console.log('Error adding item to order');
	    }
	};
	
	getOrders(callback);
    };

    var showOrdersOfTable = function (tableid = null) {
	lastTableUpdated = null;
	currentOrders = [];

	if (tableid == null) {
	    tableid = Number($('#table_selection').find(':selected').attr('value'));
	    console.log('Table selected is', tableid);
	    if (isNaN(tableid)) {
		tableid = null;
	    }
	}
	
	console.log("tableid:", tableid);
	
	getOrders({
	    onSuccess: function (payload) {
		if (tableid == null) {
		    tableid = payload[0].table_id;
		}
		addOrdersUpdateTable(payload, tableid);
	    },
	    onFailed: function (error) {
		console.log(error);
	    }});
    };

    return {
	showOrdersByTable: showOrdersByTable,
	updateOrder: updateOrder,
	deleteOrderItem: deleteOrderItem,
	addItemToOrder: addItemToOrder,
	showOrdersOfTable : showOrdersOfTable
    };

})();
