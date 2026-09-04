# Shared team OTP-sender credentials (cafeotp.noreply@gmail.com) - committed on purpose.
# Requires `java` and `mvn` already on PATH.

$env:SMTP_HOST = "smtp.gmail.com"
$env:SMTP_PORT = "587"
$env:SMTP_USERNAME = "cafeotp.noreply@gmail.com"
$env:SMTP_PASSWORD = "tgihlsgxqsjubseu"
$env:MAIL_FROM_ADDRESS = "cafeotp.noreply@gmail.com"

mvn spring-boot:run
