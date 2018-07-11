package com.mattstine.dddworkshop.pizzashop.payments;

import com.mattstine.dddworkshop.pizzashop.infrastructure.Amount;
import com.mattstine.dddworkshop.pizzashop.infrastructure.EventLog;
import com.mattstine.dddworkshop.pizzashop.ordering.OrderRef;
import lombok.Data;

/**
 * @author Matt Stine
 */
@Data
public class Payment {
	private final Amount amount;
	private final PaymentProcessor paymentProcessor;
	private final PaymentRef id;
	private final OrderRef orderRef;
	private final EventLog eventLog;
	private PaymentState paymentState;
	private boolean requested;
	private boolean successful;

	private Payment(Amount amount, PaymentProcessor paymentProcessor, PaymentRef id, OrderRef orderRef, EventLog eventLog) {
		this.amount = amount;
		this.paymentProcessor = paymentProcessor;
		this.id = id;
		this.orderRef = orderRef;
		this.eventLog = eventLog;

		this.paymentState = PaymentState.NEW;
	}

	public static PaymentBuilder of(Amount amount) {
		return new PaymentBuilder(amount);
	}

	public static PaymentBuilder withId(PaymentRef ref) {
		return new PaymentBuilder(ref);
	}

	public static PaymentBuilder withProcessor(PaymentProcessor paymentProcessor) {
		return new PaymentBuilder(paymentProcessor);
	}

	public static PaymentBuilder withOrderRef(OrderRef orderRef) {
		return new PaymentBuilder(orderRef);
	}

	public static PaymentBuilder withEventLog(EventLog eventLog) {
		return new PaymentBuilder(eventLog);
	}

	public void request() {
		if (paymentState != PaymentState.NEW) {
			throw new IllegalStateException("Payment must be NEW to request payment");
		}

		paymentProcessor.request(this);
		paymentState = PaymentState.REQUESTED;
		eventLog.publish(new PaymentRequestedEvent());
	}

	public void markSuccessful() {
		if (paymentState != PaymentState.REQUESTED) {
			throw new IllegalStateException("Payment must be REQUESTED to mark successful");
		}

		paymentState = PaymentState.SUCCESSFUL;
	}

	public boolean isNew() {
		return paymentState == PaymentState.NEW;
	}

	public boolean isRequested() {
		return paymentState == PaymentState.REQUESTED;
	}

	public boolean isSuccessful() {
		return paymentState == PaymentState.SUCCESSFUL;
	}

	public static class PaymentBuilder {
		private OrderRef orderRef;
		private Amount amount;
		private PaymentProcessor paymentProcessor;
		private PaymentRef id;
		private EventLog eventLog;

		PaymentBuilder(Amount amount) {
			this.amount = amount;
		}

		PaymentBuilder(PaymentProcessor paymentProcessor) {
			this.paymentProcessor = paymentProcessor;
		}

		PaymentBuilder(PaymentRef ref) {
			this.id = ref;
		}

		PaymentBuilder(OrderRef orderRef) {
			this.orderRef = orderRef;
		}

		PaymentBuilder(EventLog eventLog) {
			this.eventLog = eventLog;
		}

		public PaymentBuilder of(Amount amount) {
			this.amount = amount;
			return this;
		}

		public PaymentBuilder withId(PaymentRef ref) {
			this.id = ref;
			return this;
		}

		public PaymentBuilder withProcessor(PaymentProcessor paymentProcessor) {
			this.paymentProcessor = paymentProcessor;
			return this;
		}

		public PaymentBuilder withOrderRef(OrderRef orderRef) {
			this.orderRef = orderRef;
			return this;
		}

		public PaymentBuilder withEventLog(EventLog eventLog) {
			this.eventLog = eventLog;
			return this;
		}

		public Payment build() {
			if (amount == null) {
				throw new IllegalStateException("Cannot build Payment without Amount");
			}

			if (paymentProcessor == null) {
				throw new IllegalStateException("Cannot build Payment without PaymentProcessor");
			}

			if (id == null) {
				throw new IllegalStateException("Cannot build Payment without PaymentRef");
			}

			if (orderRef == null) {
				throw new IllegalStateException("Cannot build Payment without OrderRef");
			}

			if (eventLog == null) {
				throw new IllegalStateException("Cannot build Payment without EventLog");
			}

			return new Payment(amount, paymentProcessor, id, orderRef, eventLog);
		}
	}
}