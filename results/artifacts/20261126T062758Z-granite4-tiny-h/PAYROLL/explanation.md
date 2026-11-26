This COBOL program calculates and prints the weekly pay for a single employee, considering overtime, tax, and benefits based on employment status. 

The program starts by accepting an input record containing the employee ID, status (Full-time, Part-time, or Contractor), hours worked, and hourly rate. It validates the status and rejects the employee if it's not one of the three allowed values.

Next, it splits the total hours worked into regular hours (up to 40) and overtime hours (anything above 40). The regular and overtime hours are stored in separate working fields.

The gross pay is calculated based on the employment status. For contractors, the gross pay is simply the product of hours worked and hourly rate. For full-time and part-time employees, the gross pay is the sum of regular pay (regular hours multiplied by hourly rate) and overtime pay (overtime hours multiplied by 1.5 times the hourly rate).

Tax is then calculated based on the gross pay. Contractors have no tax, while full-time employees are taxed at 10% up to $500, 20% between $500 and $1500, and 30% above $1500. Part-time employees are taxed at 10% up to $500 and 20% above that.

Benefits are deducted based on employment status. Full-time employees receive a $45.50 benefit, part-time employees receive a $12.25 benefit, and contractors receive none.

Finally, the program computes the net pay by subtracting tax and benefits from the gross pay. It then prints the employee ID, status, regular hours, overtime hours, gross pay, tax, benefits, and net pay in a formatted manner.