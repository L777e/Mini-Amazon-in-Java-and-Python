import json
import socket
from .models import *

def cookieCart(request):

	#Create empty cart for now for non-logged in user
	try:
		cart = json.loads(request.COOKIES['cart'])
	except:
		cart = {}
		print('CART:', cart)

	items = []
	order = {'get_cart_total':0, 'get_cart_items':0, 'shipping':False}
	cartItems = order['get_cart_items']

	for i in cart:
		#We use try block to prevent items in cart that may have been removed from causing error
		try:	
			if(cart[i]['quantity']>0): #items with negative quantity = lot of freebies  
				cartItems += cart[i]['quantity']

				product = Product.objects.get(id=i)
				total = (product.price * cart[i]['quantity'])

				order['get_cart_total'] += total
				order['get_cart_items'] += cart[i]['quantity']

				item = {
				'id':product.id,
				'product':{'id':product.id,'name':product.title, 'price':product.price, 
				'img':product.img}, 'quantity':cart[i]['quantity'],
				'get_total':total,
				}
				items.append(item)

				if product.digital == False:
					order['shipping'] = True
		except:
			pass
			
	return {'cartItems':cartItems ,'order':order, 'items':items}

def cartData(request):
	if request.user.is_authenticated:
		customer = request.user.customer
		order, created = Order.objects.get_or_create(customer=customer, complete=False)
		items = order.orderitem_set.all()
		cartItems = order.get_cart_items
	else:
		cookieData = cookieCart(request)
		cartItems = cookieData['cartItems']
		order = cookieData['order']
		items = cookieData['items']

	return {'cartItems':cartItems ,'order':order, 'items':items}

def send_msg(package_id):
	s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
	port = 6666
	s.connect(('back-end', port))
		#connect,address = server.accept()
		#print(connect,address)
		# if UPS_id != NULL:
		# 	msg = str(package_id) + str(UPS_id) + '\n'
		# else:
	msg = str(package_id) + '\n'
	#_EncodeVarint(s.send, len(msg.encode('utf-8')), None)
	s.send(msg.encode('utf-8'))
	var_int_buff = []
	whole_message = s.recv(1024)
	print("1-------------")
	print('recv:', whole_message)
	#recv = whole_message.decode('utf-8')
	print("2-------------")
	recv = whole_message.split(whole_message)
	print("3-------------")
	print('recv:', whole_message)
	if recv[0] == "ack" and recv[1] == str(package_id):
		print("received amazon server message")
		return True
	else:
		return False


	




