package medullan.evm

import grails.test.*

class WorkItemTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
		
		def wi = mockFor(WorkItem)
		wi.demand.static.findAllWhere { -> 
			return []
        }
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testCalculatingSumInFeatureBucket() {
		def wi1 = new WorkItem(businessValue: 3)
		def wi2 = new WorkItem(businessValue: 1, parent: wi1)
		def wi3 = new WorkItem(businessValue: 2, parent: wi1)
		def wi4 = new WorkItem(businessValue: 5, parent: wi1)
		
		wi1.children = new ArrayList()
		
		wi1.children.add(wi2)
		wi1.children.add(wi3)
		wi1.children.add(wi4)
		
		assertEquals(8, wi2.aggregateValueInBucket())
		
    }
	
	void testFindingCurrentBranchOrLeafDepthInTree() {
		
		def parent = new WorkItem(businessValue: 1)
		def childRow1 = new WorkItem(businessValue: 2, parent: parent)
		def childRow2 = new WorkItem(businessValue: 4, parent: parent)
		
		parent.children = [childRow1, childRow2]
		
		def storyRow1 = new WorkItem(businessValue: 3, parent: childRow1)
		def storyRow2 = new WorkItem(businessValue: 5, parent: childRow2)
		
		childRow1.children = [storyRow1]
		childRow2.children = [storyRow2]
		
		assertEquals(2, storyRow1.depth())
	}
	
	void testFindingCurrentBranchOrLeafDepthInTreeForRootNode() {
		
		def parent = new WorkItem(businessValue: 1)
		def childRow1 = new WorkItem(businessValue: 2, parent: parent)
		def childRow2 = new WorkItem(businessValue: 4, parent: parent)
		
		parent.children = [childRow1, childRow2]
		
		def storyRow1 = new WorkItem(businessValue: 3, parent: childRow1)
		def storyRow2 = new WorkItem(businessValue: 5, parent: childRow2)
		
		childRow1.children = [storyRow1]
		childRow2.children = [storyRow2]
		
		assertEquals(0, parent.depth())
	}
	
	void testFindingAggregateValueForParentLevelInTree() {
		
		def parent = new WorkItem(businessValue: 1)
		def childRow1 = new WorkItem(businessValue: 2, parent: parent)
		def childRow2 = new WorkItem(businessValue: 4, parent: parent)
		
		parent.children = [childRow1, childRow2]
		
		def storyRow1 = new WorkItem(businessValue: 3, parent: childRow1)
		def storyRow2 = new WorkItem(businessValue: 5, parent: childRow2)
		
		childRow1.children = [storyRow1]
		childRow2.children = [storyRow2]
		
		def aggregateExpectedValue = 6
		
	}
	
	void testCalculatingAdditiveValue() {
		
		def wi = mockFor(WorkItem)
		def wi0
		
		wi.demand.static.findBy(100) { args ->
			return wi0
		}
		
		// top level
		wi0 = new WorkItem(businessValue: 1)
		
		// features
		def wi1 = new WorkItem(businessValue: 1, parent: wi0)
		def wi2 = new WorkItem(businessValue: 4, parent: wi0)
		
		wi0.children = [wi1, wi2]
		
		// epics
		def wi3 = new WorkItem(businessValue: 4, parent: wi1)
		def wi4 = new WorkItem(businessValue: 8, parent: wi1)
	
		wi1.children = [wi3, wi4]
		
		// stories
		def wi5 = new WorkItem(businessValue: 1, parent: wi3)
		def wi6 = new WorkItem(businessValue: 34, parent: wi3)
		
		wi3.children = [wi5, wi6]
		
		// todo - need to recurse here
		Number expected = (1/35) * (4/12) * (1/5) //  = .001904762		
		
		assertEquals(expected, wi5.additiveValueInWorkBreakdown())
	}
	
	void testCalculatingParentTrancheValue() {
		def wi0 = new WorkItem(businessValue: 1, name: "Product 1")
		
		// features
		def wi1 = new WorkItem(businessValue: 1, parent: wi0, name: "Feature 1")
		def wi2 = new WorkItem(businessValue: 4, parent: wi0, name: "Feature 2")
		
		wi0.children = [wi1, wi2]
		
		def wi = mockFor(WorkItem)
		def calls = 0
		// TODO the expectation below is because i'm too lazy to do the permutative math
		wi.demand.static.findBy(300) { args ->
			return wi0
		}
		
		// epics
		def wi3 = new WorkItem(businessValue: 4, parent: wi1, name: "Epic 1")
		def wi4 = new WorkItem(businessValue: 8, parent: wi1, name: "Epic 2")
		def wi4a = new WorkItem(businessValue: 6, parent: wi2, name: "Epic 3")
	
		wi1.children = [wi3, wi4]
		wi2.children = [wi4a]
		
		// stories
		def wi5 = new WorkItem(businessValue: 1, parent: wi3, name: "Story 1")
		def wi6 = new WorkItem(businessValue: 34, parent: wi4, name: "Story 2")
		
		wi3.children = [wi5]
		
		def expected = 18
		assertEquals(expected, wi5.valueOfParentTranch())
		assertEquals(1, wi1.valueOfParentTranch())
		assertEquals(null, wi0.valueOfParentTranch()) // the root node has no parent tranch
	}
	
	
}
