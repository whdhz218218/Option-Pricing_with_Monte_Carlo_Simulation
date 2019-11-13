package exchangeStructures;

import java.util.*;
import fills.Fill;
import orderSpecs.*;
import orderTypes.*;

public class PriceLevel {
	private LinkedList<RestingOrder> _ordersList;
	/**
	 * important!! Notice that inner class can call the outside class through making it as a field!!!
	 */
	private Book _Book;
	
	public PriceLevel(Book book) {
		_ordersList=new LinkedList<RestingOrder>();
		_Book=book;
	}

	public LinkedList<RestingOrder> getOrders() {
		return _ordersList;
	}
	
	public void addOrders(SweepingOrder sweepingOrder ) {
		RestingOrder ro=new RestingOrder(sweepingOrder);
		_ordersList.add(ro);
	}
	
	public void removeOrders(int orderIndex) {
		_ordersList.remove(orderIndex);
	}
	
	
	/** Add Fill message by calling the exchange addfill method
	 * @param restingOrder
	 * @param sweepingOrder
	 * @param q
	 */
	public void setFill(RestingOrder restingOrder, SweepingOrder sweepingOrder, Quantity q)  {
		Fill fill1=new Fill(sweepingOrder.getClientId(),restingOrder.getClientId(),sweepingOrder.getClientOrderId(),q);
		Fill fill2=new Fill(restingOrder.getClientId(),sweepingOrder.getClientId(),restingOrder.getClientOrderId(),q);
	    this._Book.getMarket().getExchange().addCommsFill(fill2);	
	    this._Book.getMarket().getExchange().addCommsFill(fill1);
	}
	
	
	/** First, check whether if the corresponding order needs to be cancelled through checking the 
	 * exchange ordermap. Second, check if the sweepingorder has zero quantity. Third, determine
	 * who has the smaller quantity then reduce by the quantity. Last, remove the resting order if 
	 * it has zero quantity
	 * @param sweepingOrder
	 * @throws Exception
	 */
	public void sweep(SweepingOrder sweepingOrder) throws Exception {
		while(_ordersList.size()!=0) {
			if (this._Book.getMarket().getExchange().getOrder(_ordersList.get(0).getClientOrderId())==null) {
				_ordersList.remove(0);
				continue;	
			}
			if(sweepingOrder.isFilled()) {break;}
			Quantity q=new Quantity(sweepingOrder.getQuantity());
			if(sweepingOrder.getQuantity().compareTo(_ordersList.get(0).getQuantity())>=0) {
				q=new Quantity(_ordersList.get(0).getQuantity());
				sweepingOrder.reduceQtyBy(_ordersList.get(0).getQuantity());
				_ordersList.get(0).reduceQtyBy(_ordersList.get(0).getQuantity());
			}
			else {	
				_ordersList.get(0).reduceQtyBy(sweepingOrder.getQuantity());
				sweepingOrder.reduceQtyBy(sweepingOrder.getQuantity());
			}
			setFill(_ordersList.get(0), sweepingOrder, q);    
		    if(_ordersList.get(0).isFilled()) {
		    	this._Book.getMarket().getExchange().removeOrder(_ordersList.get(0));
		    	_ordersList.remove(0);
			}
		}		
	}
	
}
