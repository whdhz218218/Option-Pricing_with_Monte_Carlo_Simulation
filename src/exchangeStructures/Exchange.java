package exchangeStructures;

import java.util.*;

import fills.Fill;
import messages.Cancel;
import messages.CancelRejected;
import messages.Cancelled;
import messages.RestingOrderConfirmation;
import orderSpecs.*;
import orderTypes.*;

public class Exchange {
	
	private HashMap<MarketId, Market> _marketMap;
	private HashMap<ClientOrderId, RestingOrder> _orderMap;
	private Comms _comms;	
	
	public Exchange() {
		_marketMap = new  HashMap<MarketId, Market>();
		_orderMap = new HashMap<ClientOrderId, RestingOrder>();
		_comms = new Comms();	
	}

	public void addMarket(Market market) {
		_marketMap.put(market.getMarketId(),market);
	}

	public Market getMarket(MarketId marketId0) {
		return _marketMap.get(marketId0);
	}	
	
	public RestingOrder getOrder(ClientOrderId clientOrderId) {
		return _orderMap.get(clientOrderId);
	}
	
	public void addOrder(SweepingOrder sweepingOrder) {
		RestingOrder restingOrder=new RestingOrder(sweepingOrder);
		_orderMap.put(restingOrder.getClientOrderId(), restingOrder);
	}
	
	public RestingOrder removeOrder(RestingOrder restingOrder) {
		return _orderMap.remove(restingOrder.getClientOrderId());
	}
	
	public Comms getComms() {
		return _comms;
	}
	
	public void addCommsROConfirmation(RestingOrder restingOrder) {
		RestingOrderConfirmation restingOrderConfirmation=new RestingOrderConfirmation(restingOrder );
		_comms.sendRestingOrderConfirmation(restingOrderConfirmation);
	}
	
	public void addCancel(Cancel cancel) {
		Cancelled cancelled = new Cancelled(cancel.getClientId(), cancel.getClientOrderId());
		_comms.cancelled(cancelled);
	}
	
	public void addCancelRejected(Cancel cancel) {
		CancelRejected rejectMsg = new CancelRejected(cancel.getClientId(), cancel.getClientOrderId());
		_comms.sendCancelRejected(rejectMsg );
	}
			
	public void addCommsFill(Fill fill) {
		_comms.sendFill(fill);
	}
	
	
	/** First check if there is the market, unless create one, then direct to the market
	 * @param sweepingOrder the order needs to be put into the pricelevel
	 * @throws Exception
	 */
	public void sweep(SweepingOrder sweepingOrder) throws Exception {
		if(!_marketMap.containsKey(sweepingOrder.getMarketId())){
			Market market=new Market(this, sweepingOrder.getMarketId());
			this.addMarket(market);		
		}
		//call sweep method in Book class
		_marketMap.get(sweepingOrder.getMarketId()).sweep(sweepingOrder);
	}	
	
	
	/** Remove the order from the orderMap if it exists; unless send reject message 
	 * @param cancel
	 */
	public void cancel(Cancel cancel) {
		if(_orderMap.containsKey(cancel.getClientOrderId())) {
			_orderMap.remove(cancel.getClientOrderId());
			addCancel(cancel);
		}
		else{
			addCancelRejected(cancel);
		}
	}
	
	
}
