package medullan.evm

/**
 * A {@link WorkItem} represents a component of decomposition in a work breakdown structure.  Each object can be deivided into distinct trees of work as needed.  This object also provides facilities for calculating the net business value of each object in it's hierarchy.
 * @author rnorris
 *
 */
class WorkItem {

	List<WorkItem> children

	static hasMany = [ children : WorkItem ]
	static belongsTo = WorkItem

	static constraints = {
	}

	String name
	Number businessValue
	WorkItem parent

	def rootNode() {
		WorkItem.findBy(parent: null)
	}

	/**
	 * Calculate the value of the items in this bucket (the value of all terminating leaves in a branch)
	 * TODO is this right? Should we rename?
	 * @return
	 */
	Number aggregateValueInBucket() {
		if(parent) {
			return parent.children*.businessValue.sum()
		}

		return WorkItem.findAllWhere(parent: null)*.businessValue.sum()
	}

	/**
	 * Calculates the depth of the node in the work tree
	 * @return a number that represents the depth (distance) of this WorkItem from the root node
	 */
	Number depth() {
		def current = this
		def depth = 0

		while((current = current.parent) != null) {
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

			def currentTranch = rootNode.children
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

		collectTranches(rootNode(), depth())
		tranchMap[depth() - 1]*.businessValue.sum()
	}

	Number additiveValueInWorkBreakdown() {
		def businessValue = businessValue / aggregateValueInBucket()
		def parentTranchValue = valueOfParentTranch()
		WorkItem current = this

		while((current = current.parent) != null) {
			def tranchValue = current.businessValue / parentTranchValue
			businessValue *= tranchValue
			parentTranchValue = current.valueOfParentTranch()
		}

		businessValue
	}
}
