package org.to2mbn.jmccc.mojangapi;

import java.io.Serializable;
import java.util.Objects;

public class SalesStatistics implements Serializable {

	public static final class MetricKeys {

		public static final String ITEM_SOLD_MINECRAFT = "item_sold_minecraft";
		public static final String PREPAID_CARD_REDEEMED_MINECRAFT = "prepaid_card_redeemed_minecraft";
		public static final String ITEM_SOLD_COBALT = "item_sold_cobalt";
		public static final String ITEM_SOLD_SCROLLS = "item_sold_scrolls";

		private MetricKeys() {}
	}

	private static final long serialVersionUID = 1L;

	private long total;
	private long last24h;
	private long saleVelocityPerSeconds;

	public SalesStatistics(long total, long last24h, long saleVelocityPerSeconds) {
		this.total = total;
		this.last24h = last24h;
		this.saleVelocityPerSeconds = saleVelocityPerSeconds;
	}

	/**
	 * @return total amount sold
	 */
	public long getTotal() {
		return total;
	}

	/**
	 * @return total sold in last 24 hours
	 */
	public long getLast24h() {
		return last24h;
	}

	/**
	 * @return average sales per second
	 */
	public long getSaleVelocityPerSeconds() {
		return saleVelocityPerSeconds;
	}

	@Override
	public String toString() {
		return String.format("SalesStatistics [total=%s, last24h=%s, saleVelocityPerSeconds=%s]", total, last24h, saleVelocityPerSeconds);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof SalesStatistics) {
			SalesStatistics another = (SalesStatistics) obj;
			return total == another.total
					&& last24h == another.last24h
					&& saleVelocityPerSeconds == another.saleVelocityPerSeconds;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(total, last24h, saleVelocityPerSeconds);
	}

}
