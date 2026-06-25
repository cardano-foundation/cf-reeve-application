Feature: Users can reprocess transactions that are in pending status once the issue is solved, system checks
  reevaluate pending reason to change transaction status.

  Scenario: Reprocess transaction in pending status by cost center mapping
    Given there is an imported transaction in pending status due to cost center mapping
    And the issue with the cost center is adjusted in the system
    When the system process the reprocess request
    Then the transaction should change the status to ready to approve