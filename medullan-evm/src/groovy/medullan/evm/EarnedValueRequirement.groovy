/**
 * 
 */
package medullan.evm

import grails.plugin.rally.HierarchicalRequirement
import grails.plugin.rally.Iteration
import grails.plugin.rally.Project
import grails.plugin.rally.RallyConnectionFactory
import grails.plugin.rally.Workspace

import org.apache.commons.logging.LogFactory


/**
 * @author rnorris
 *
 */

enum ValueType {
	Planned, Earned
}

class EarnedValueRequirement extends HierarchicalRequirement {

	private static final log = LogFactory.getLog(this)
	
	def bucketValue
	def treeDepth
	def parentTranchValue
	def additiveWBValue
	
	public EarnedValueRequirement(String storyId, String storyName, RallyConnectionFactory factory, Boolean initOnLoad = false) {
		super(storyId, storyName, factory, initOnLoad)
	}
	
	protected String getObjectName() {
		return "HierarchicalRequirement"
	}
	
	def value = { ValueType type ->
		String valueField
		
		if(type == ValueType.Planned) {
			valueField = 'Value'
		} else {
			valueField = 'EarnedValue'
		}
		
		return get(valueField, 0)
	}
	
	def rootNodes() {
		return query("((Parent = null) AND (Project = ${project().getObjectId()}))").results
	}
	
	def rootNode() {
		def parentStory = parent()
		log.trace("parent is ${parentStory?.getObjectTitle()}")
		def lastParent = parentStory
		
		if(! parentStory) {
			log.trace("there is no parent, this is the root story (${getObjectTitle()})")
			return this
		}
		
		while((parentStory = parentStory.parent()) != null) {
			log.trace("next possible root node is ${parentStory?.getObjectTitle()}")
			lastParent = parentStory
		}
		
		log.trace("the parent story before the root was ${parentStory?.getObjectTitle()}")
		log.debug("root node for this story is ${lastParent?.getObjectTitle()}")
		
		lastParent
	}
	
	Number aggregateValueInBucket(ValueType type) {
		
		log.debug("Using value type ${type}")
		
		if(parent()) {
			// return parent().children()*.get(valueField, 0).sum()
			return parent().children()*.value(type).sum()
		}
		
		def roots = rootNodes()
		log.trace("--- root nodes found ---")
		roots.each { node ->
			log.trace(node.getObjectTitle())
		}
		log.trace("--- done with root nodes --")

		// bucketValue = roots*.get(valueField, 0).sum()
		bucketValue = roots*.value(type).sum()
	}	
	
	Number depth() {
		def current = this
		def depth = 0

		while((current = current.parent()) != null) {
			// work up the branches to the trunk
			depth++
		}

		return depth
	}
	
	Number valueOfParentTranch() {
		
		if(depth() < 1) {
			return null
		}

		def collectTranches
		def tranchMap = []

		collectTranches = { rootNode, depth ->

			def currentTranch = rootNode.children()
			if(! tranchMap[rootNode.depth()]) {
				tranchMap[rootNode.depth()] = []
			}

			tranchMap[rootNode.depth()] += rootNode

			if(rootNode.depth() < depth) {
				currentTranch.each { it ->
					collectTranches(it, depth)
				}
			}
		}

		log.debug("root for ${getObjectTitle()}")
		collectTranches(rootNode(), depth())
		// parentTranchValue = tranchMap[depth() - 1]*.get(valueField, 0).sum()
		parentTranchValue = tranchMap[depth() - 1]*.value(ValueType.Planned).sum()
		
	}

	Number additiveValueInWorkBreakdown(ValueType type) {
		def assignedValue = value(type)
		
		if(! assignedValue) {
			return 0
		}
		
		def bucketValue = aggregateValueInBucket(type)
		def parentTranchValue = valueOfParentTranch()
		
		def businessValue = assignedValue / bucketValue
		
		log.debug("--- calculating business value ---")
		log.debug("business value of story = ${assignedValue} / ${bucketValue}")
		
		def current = this

		while((current = current.parent()) != null) {
			log.debug("next parent business value = ${current.value(ValueType.Planned)} / ${current.aggregateValueInBucket(ValueType.Planned)}")
			
			def parentValue = (current.aggregateValueInBucket(ValueType.Planned)) ? current.value(ValueType.Planned) / current.aggregateValueInBucket(ValueType.Planned) : 0
			
			// log.debug("next parent business value = ${current.value(type)} / ${current.aggregateValueInBucket(type)}")
			businessValue *= parentValue
		}
		
		log.debug("--- done calculating business value ---")

		businessValue
	}
}

class ValueMeasuredIteration extends Iteration {

	private static final log = LogFactory.getLog(this)
	
	EarnedValueRequirement evr
	
	public ValueMeasuredIteration(String storyId, String storyName, RallyConnectionFactory factory, Boolean initOnLoad = false) {
		super(storyId, storyName, factory, initOnLoad);
		
		evr = new EarnedValueRequirement(null, null, factory, false)
	}
	
	protected String getObjectName() {
		return "Iteration"
	}
	
	def plannedStories = {
		evr.query("((Iteration = ${getObjectId()}) AND (Project = ${project().getObjectId()}))", 100, 1, null, true).results
	}
	
	def deliveredStories = {
		evr.query("(((Iteration = ${getObjectId()}) AND (Project = ${project().getObjectId()})) AND (ScheduleState = Accepted))", 100, 1, null, true).results
	}
	
	def plannedValue = {
		def planned = plannedStories()
		
		if(! planned) {
			return 0
		}
		
		log.debug(" --- calculating planned value --- ")
		planned*.additiveValueInWorkBreakdown(ValueType.Planned).sum() * 100
	}
	
	def earnedValue = {
		def delivered = deliveredStories()
		
		if(! delivered) {
			return 0
		}
		
		log.debug(" --- calculating earned value --- ")
		delivered*.additiveValueInWorkBreakdown(ValueType.Earned).sum() * 100
	}
}

class ValueMeasuredProject extends Project {
	
	public ValueMeasuredProject(String projectId, String projectName, RallyConnectionFactory factory, Boolean initOnLoad = false) {
		super(projectId, projectName, factory, initOnLoad)
	}
	
	protected String getObjectName() {
		return "Project"
	}
	
	def iterations() {
		def iterations = []
		
		get('Iterations').each { iteration ->
			ValueMeasuredIteration projectIteration = new ValueMeasuredIteration(iteration._ref, iteration._refName, connectionFactory, false)
			iterations += projectIteration
		}
		
		iterations
	}
}

class ValueMeasuredWorkspace extends Workspace {
	public ValueMeasuredWorkspace(String workspaceId, RallyConnectionFactory factory) {
		super(workspaceId, factory)
	}
	
	protected String getObjectName() {
		return "Workspace"
	}
	
	def projects() {
		def projects = []
		
		get('Projects').each { project ->
			ValueMeasuredProject workspaceProject = new ValueMeasuredProject(project._ref, project._refName, connectionFactory, false)
			projects += workspaceProject
		}
		
		projects
	}
}
