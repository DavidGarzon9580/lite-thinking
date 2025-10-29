output "s3_bucket_name" {
  description = "Bucket creado para inventarios."
  value       = aws_s3_bucket.inventory.bucket
}

output "ses_sender_status" {
  description = "Estado de verificacion del remitente."
  value       = var.ses_sender_email != "" ? aws_ses_email_identity.sender[0].email : "no-configured"
}
