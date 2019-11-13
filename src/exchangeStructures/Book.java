package exchangeStructures;

import java.util.*;
import orderSpecs.*;
import orderTypes.*;

public class Book {
	private Market _market;
	private Side _side;
	private Book _otherSide;
	private TreeMap<Price,PriceLevel> _pricelevelTreeMap ;
	
	
	public Book(Market market, Side s) {
		_market=market;
		_side=s;
		_pricelevelTreeMap=new TreeMap<Price,PriceLevel>(this._side.getComparator());
	}
	
	public void setOtherSide(Book otherSide) {
		this._otherSide=otherSide;
	}

	public Book getOtherSide() {
		return _otherSide;
	}
	
	public Market getMarket() {
		return _market;
	}
	
	public Side getSide() {
		return _side;
	}
	
	public TreeMap<Price,PriceLevel> getPriceLevels() {
		return _pricelevelTreeMap;
	}

	
    /** Check if the first key in the pricelevelTreeMap is corresponding to sweeping order,
     * remember the key price or make it null
     * @param sweepingOrder
     * @return
     */
	public Price checkFirstKey(SweepingOrder sweepingOrder) {
		Price key=null;
		if(this._pricelevelTreeMap.size()!=0) {
			if(this._side == Side.SELL){
				if(this._pricelevelTreeMap.firstKey().compareTo(sweepingOrder.getPrice())<=0) {key=new Price(this._pricelevelTreeMap.firstKey());}
			}
			else {
				if(this._pricelevelTreeMap.firstKey().compareTo(sweepingOrder.getPrice())>=0) {key=new Price(this._pricelevelTreeMap.firstKey());}
			}	
		}
		return key;
	}
	
    /** Create the price and corresponding price level if there is no such price in the treemap,
     * then add the sweeping order into the corresponding price level, and then send message since
     * we add resting order into treemap 
     * @param sweepingOrder
     */
	public void addOrder(SweepingOrder sweepingOrder) {
		if(!this._pricelevelTreeMap.containsKey(sweepingOrder.getPrice())) {
			this._pricelevelTreeMap.put(sweepingOrder.getPrice(), new PriceLevel(this));
		}
		this._pricelevelTreeMap.get(sweepingOrder.getPrice()).addOrders(sweepingOrder);
		this._market.getExchange().addCommsROConfirmation(new RestingOrder(sweepingOrder));	
	}
	
	/** Direct the method into pricelevel sweep until there is no corresponding key or the sweeping
	 * order has zero quantity. If at last,  the sweeping order has non-zero quantity, we add it to 
	 * pricelevelTreeMap of the other side and the exchange ordermap
	 * @param sweepingOrder
	 * @throws Exception
	 */
	public void sweep(SweepingOrder sweepingOrder) throws Exception{		
		Price key=checkFirstKey( sweepingOrder);	
		while(key!=null && !sweepingOrder.isFilled()) {	
			this._pricelevelTreeMap.get(key).sweep(sweepingOrder);
			if(this._pricelevelTreeMap.get(key).getOrders().size()==0) {this._pricelevelTreeMap.remove(key);}
			key=checkFirstKey( sweepingOrder);			
		}
		if(!sweepingOrder.isFilled()) {
			this._otherSide.addOrder(sweepingOrder);
			this._market.getExchange().addOrder(sweepingOrder);
		}	
	}


}
