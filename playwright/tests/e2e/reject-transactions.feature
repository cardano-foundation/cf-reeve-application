Feature: Users can reject transactions that has wrong data to avoid be published or to be correct it, system checks
  rejection reason to change transaction status.

  Scenario: Reject transaction to invalid by incorrect cost center
    Given user import ready to approve transaction to the system
    And And the user wants to rejects the transaction by incorrect cost center
    When the system get the rejection request
    Then the transaction should be now in invalid status by "INCORRECT_COST_CENTER" reason