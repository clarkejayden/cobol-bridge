This COBOL program calculates the weekly pay for an employee, considering overtime, tax brackets, and benefits deductions based on employment status. Here's a breakdown:

### Data Layout
- **Input Record (`IN-REC`)**:
  - `IN-EMP-ID`: Employee ID (6 characters)
  - `IN-STATUS`: Employment status ('F' for Full-Time, 'P' for Part-Time, 'C' for Contractor)
  - `IN-HOURS`: Hours worked (3 digits, packed-decimal)
  - `IN-RATE`: Hourly rate (3 digits, packed-decimal with 2 decimal places)

- **Working Fields**:
  - `WS-REG-HOURS`: Regular hours (3 digits, packed-decimal)
  - `WS-OT-HOURS`: Overtime hours (3 digits, packed-decimal)
  - `WS-GROSS`: Gross pay (7 digits, packed-decimal with 2 decimal places)
  - `WS-TAX`: Tax amount (7 digits, packed-decimal with 2 decimal places)
  - `WS-BENEFITS`: Benefits deduction (3 digits, packed-decimal with 2 decimal places)
  - `WS-NET`: Net pay (7 digits, packed-decimal with 2 decimal places)

### Business Logic
1. **Validation**:
   - Checks if the employment status is valid ('F', 'P', or 'C'). If not, it rejects the employee and stops the program.

2. **Split Hours**:
   - Splits the total hours into regular hours and overtime hours. Overtime is calculated at time-and-a-half (1.5 times the regular rate) if the employee is not a contractor.

3. **Calculate Gross Pay**:
   - Calculates gross pay based on the number of regular and overtime hours. Contractors are paid based on total hours worked.

4. **Calculate Tax**:
   - Calculates tax based on the gross pay. Tax brackets are:
     - 10% for gross pay up to $500
     - 20% for gross pay between $501 and $1500
     - 30% for gross pay above $1500
   - Contractors are exempt from tax.

5. **Calculate Benefits**:
   - Deducts benefits based on employment status:
     - Full-Time: $45.50
     - Part-Time: $12.25
     - Contractors: $0

6. **Calculate Net Pay**:
   - Net pay is calculated by subtracting tax and benefits from gross pay.

### Output
The program prints a payroll stub with the following details:
- Employee ID
- Employment status
- Regular hours
- Overtime hours
- Gross pay
- Tax amount
- Benefits deduction
- Net pay

### Example Output
For an employee with ID "123456", status "F", 45 hours worked, and a rate of $20.00:
```
EMPLOYEE  123456  STATUS F
REG HOURS 40.0
OT HOURS  5.0
GROSS     950.00
TAX       140.00
BENEFITS  45.50
NET PAY   764.50
```

This output is formatted to match the packed-decimal representation used in the COBOL program.