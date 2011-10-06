package medullan.evm

import org.springframework.context.ApplicationListener;

class ValueModelCalculatorService implements ApplicationListener<ValueModelCalculationEvent> {

    static transactional = true

	void onApplicationEvent(ValueModelCalculationEvent event) {
		event.source.lastQuery = [:]
		event.source.lastQuery.complete = false
		
		event.iteration.plannedStories().each {	eachStory ->
			eachStory.getObjectId()
			eachStory.getObjectTitle()
			eachStory.value()
			eachStory.additiveValueInWorkBreakdown()
		}
		
		event.source.lastQuery.result = event.iteration
		event.source.lastQuery.complete
	}
}
