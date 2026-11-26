This COBOL program calculates a weekly payroll for an employee, considering overtime, marginal tax brackets, and benefits based on employment status. Here's a concise explanation:

1. Data Layout:
   - Input record: `IN-REC` containing employee ID (`IN-EMP-ID`), status (`IN-STATUS`), hours worked (`IN-HOURS`), and hourly rate (`IN-RATE`).
   - Working fields: `WS-REG-HOURS`, `WS-OT-HOURS`, `WS-GROSS`, `WS-TAX`, `WS-BENEFITS`, `WS-NET`, `OUT-HOURS`, `OUT-AMT`, `OUT-NET`.

2. Business Logic:
   - Accept input record.
   - Reject if status is invalid.
   - Split hours worked into regular and overtime based on 40-hour threshold.
   - Calculate gross pay based on regular hours and overtime (time-and-a-half for non-contractors).
   - Calculate tax based on gross pay and marginal tax brackets.
   - Calculate benefits based on employment status.
   - Compute net pay by subtracting tax and benefits from gross pay.
   - Print the employee ID, status, regular hours, overtime hours, gross pay, tax, benefits, and net pay.

3. Output:
   - Employee ID, status, regular hours, overtime hours, gross pay, tax, benefits, and net pay are printed in a formatted manner.

The program uses packed-decimal work fields for efficient storage and calculations, similar to a typical batch payroll system. The output is compared byte for byte with the original COBOL program to ensure behavior is identical.