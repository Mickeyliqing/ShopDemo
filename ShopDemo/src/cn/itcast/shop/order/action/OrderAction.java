package cn.itcast.shop.order.action;

import java.util.Date;

import org.apache.struts2.ServletActionContext;

import cn.itcast.shop.cart.vo.Cart;
import cn.itcast.shop.cart.vo.CartItem;
import cn.itcast.shop.order.service.OrderService;
import cn.itcast.shop.order.vo.Order;
import cn.itcast.shop.order.vo.OrderItem;
import cn.itcast.shop.untils.PageBean;
import cn.itcast.shop.untils.PaymentUtil;
import cn.itcast.shop.user.vo.User;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

@SuppressWarnings("serial")
public class OrderAction extends ActionSupport implements ModelDriven<Order>{

	private Order order = new Order();

	public Order getModel() {
		// TODO Auto-generated method stub
		return order;
	}
	
	private OrderService orderService;

	public void setOrderService(OrderService orderService) {
		this.orderService = orderService;
	}
	
	private Integer page;
	
	public void setPage(Integer page) {
		this.page = page;
	}

	private String pd_FrpId;
	
	public void setPd_FrpId(String pd_FrpId) {
		this.pd_FrpId = pd_FrpId;
	}

	private String r6_Order;
	
	public void setR6_Order(String r6_Order) {
		this.r6_Order = r6_Order;
	}

	private String r3_Amt;
	
	public void setR3_Amt(String r3_Amt) {
		this.r3_Amt = r3_Amt;
	}

	public String saveOrder(){
		// 设置订单的总金额:订单的总金额应该是购物车中总金额:
		// 购物车在session中,从session总获得购物车信息.
		Cart cart = (Cart) ServletActionContext.getRequest().getSession().getAttribute("cart");
		if(cart == null){
			this.addActionMessage("亲！你还没有购物！");
			return "msg";
		}
		order.setTotal(cart.getTotal());
		order.setState(1);
		order.setOrdertime(new Date());
		// 设置订单关联的客户:
		User user = (User) ServletActionContext.getRequest().getSession().getAttribute("existUser");
		if(user == null){
			this.addActionMessage("亲！你还没有登录！");
			return "msg";
		}
		order.setUser(user);
		// 设置订单项集合:
		for (CartItem cartItem : cart.getCartItems()) {
			// 订单项的信息从购物项获得的.
			OrderItem orderItem = new OrderItem();
			orderItem.setCount(cartItem.getCount());
			orderItem.setSubtotal(cartItem.getSubtotal());
			orderItem.setProduct(cartItem.getProduct());
			orderItem.setOrder(order);
			
			order.getOrderItems().add(orderItem);
		}
		orderService.save(order);
		cart.clearCart();
		// 页面需要回显订单信息:
		// 使用模型驱动了 所有可以不使用值栈保存了
		// ActionContext.getContext().getValueStack().set("order", order);
		return "saveOrder";
	}
	
	public String findByUid(){
		User user = (User) ServletActionContext.getRequest().getSession().getAttribute("existUser");
		if(user == null){
			this.addActionMessage("亲！你还没有登录！");
			return "msg";
		}
		Integer uid = user.getUid();
		PageBean<Order> pageBean = orderService.findByUid(uid,page);
		ActionContext.getContext().getValueStack().set("pageBean", pageBean);
		return "findByUid";
	}
	
	public String findByOid(){
		order = orderService.findByOid(order.getOid());
		return "findByOid";
	}
	
	// 为订单付款:
		public String payOrder() throws Exception {
			// 1.修改数据:
			Order currOrder = orderService.findByOid(order.getOid());
			currOrder.setAddr(order.getAddr());
			currOrder.setName(order.getName());
			currOrder.setPhone(order.getPhone());
			// 修改订单
			orderService.update(currOrder);
			// 2.完成付款:
			// 付款需要的参数:
			String p0_Cmd = "Buy"; // 业务类型:
			String p1_MerId = "10001126856";// 商户编号:
			String p2_Order = order.getOid().toString();// 订单编号:
			String p3_Amt = "0.01"; // 付款金额:
			String p4_Cur = "CNY"; // 交易币种:
			String p5_Pid = ""; // 商品名称:
			String p6_Pcat = ""; // 商品种类:
			String p7_Pdesc = ""; // 商品描述:
			String p8_Url = "http://192.168.2.111:8080/ShopDemo/order_callBack.action"; // 商户接收支付成功数据的地址:
			String p9_SAF = ""; // 送货地址:
			String pa_MP = ""; // 商户扩展信息:
			String pd_FrpId = this.pd_FrpId;// 支付通道编码:
			String pr_NeedResponse = "1"; // 应答机制:
			String keyValue = "69cl522AV6q613Ii4W6u8K6XuW8vM1N6bFgyv769220IuYe9u37N4y7rI4Pl"; // 秘钥
			String hmac = PaymentUtil.buildHmac(p0_Cmd, p1_MerId, p2_Order, p3_Amt,
					p4_Cur, p5_Pid, p6_Pcat, p7_Pdesc, p8_Url, p9_SAF, pa_MP,
					pd_FrpId, pr_NeedResponse, keyValue); // hmac
			// 向易宝发送请求:
			StringBuffer sb = new StringBuffer("https://www.yeepay.com/app-merchant-proxy/node?");
			sb.append("p0_Cmd=").append(p0_Cmd).append("&");
			sb.append("p1_MerId=").append(p1_MerId).append("&");
			sb.append("p2_Order=").append(p2_Order).append("&");
			sb.append("p3_Amt=").append(p3_Amt).append("&");
			sb.append("p4_Cur=").append(p4_Cur).append("&");
			sb.append("p5_Pid=").append(p5_Pid).append("&");
			sb.append("p6_Pcat=").append(p6_Pcat).append("&");
			sb.append("p7_Pdesc=").append(p7_Pdesc).append("&");
			sb.append("p8_Url=").append(p8_Url).append("&");
			sb.append("p9_SAF=").append(p9_SAF).append("&");
			sb.append("pa_MP=").append(pa_MP).append("&");
			sb.append("pd_FrpId=").append(pd_FrpId).append("&");
			sb.append("pr_NeedResponse=").append(pr_NeedResponse).append("&");
			sb.append("hmac=").append(hmac);
			
			// 重定向:向易宝出发:
			ServletActionContext.getResponse().sendRedirect(sb.toString());
			return NONE;
		}
		//付款成功后跳转的路径
		public String callBack(){
			//修改订单状态
			Order currorder = orderService.findByOid(Integer.parseInt(r6_Order));
			currorder.setState(2);
			orderService.update(currorder);
			this.addActionMessage("支付成功!订单编号为: "+r6_Order +" 付款金额为: "+r3_Amt);
			return "msg";
		}
		
		public String updateState(){
			Order currorder = orderService.findByOid(order.getOid());
			currorder.setState(4);
			orderService.update(currorder);
			return "updateState";
		}
}
