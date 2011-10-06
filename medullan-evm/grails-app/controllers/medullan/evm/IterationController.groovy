package medullan.evm

import grails.plugin.rally.RallyConnectionFactory


class IterationController {

	def read = {

		RallyConnectionFactory factory = new RallyConnectionFactory();
		ValueMeasuredIteration theIteration = new ValueMeasuredIteration(params.id, null, factory)

		publishEvent(new ValueModelCalculationEvent(theIteration, session))

		withFormat {
			json {
				log.debug("last query still running: ${session.lastQuery?.complete ? 'Yes' : 'No'}")
				if(session.lastQuery?.complete) {
					def result = session.lastQuery?.result
					
					render(contentType: 'application/json') {
						query(state: 'complete')
						iteration(title: result.getObjectTitle(), plannedValue: result.plannedValue(), earnedValue: result.earnedValue())
						stories = array {
							result.plannedStories().each {
								eachStory ->
								story(id: eachStory.getObjectId(),
								name: eachStory.getObjectTitle(),
								value: eachStory.value(),
								additiveValue: eachStory.additiveValueInWorkBreakdown()
								)
							}
						}
					} 
				} else {
					render(contentType: 'application/json', status: 202) {
						query(state: 'incomplete')
					}
				}
			}
		}
	}
}