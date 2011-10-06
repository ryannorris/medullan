/**
 * 
 */
package medullan.evm

import org.springframework.context.ApplicationEvent;

/**
 * @author rnorris
 *
 */
class ValueModelCalculationEvent extends ApplicationEvent {
	
	def iteration
	
	ValueModelCalculationEvent(ValueMeasuredIteration iteration, def source) {
		super(source)
		
		this.iteration = iteration
	}
}
