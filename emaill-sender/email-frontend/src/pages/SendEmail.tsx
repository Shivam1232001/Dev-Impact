import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useNavigate } from "react-router-dom";
import { ArrowLeft, Send, Paperclip, X } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";

const emailSchema = z.object({
  to: z.string().email({ message: "Please enter a valid email address" }),
  subject: z.string().min(1, { message: "Subject is required" }).max(200, { message: "Subject must be less than 200 characters" }),
  message: z.string().min(1, { message: "Message is required" }).max(5000, { message: "Message must be less than 5000 characters" }),
});

type EmailFormData = z.infer<typeof emailSchema>;

const SendEmail = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [files, setFiles] = useState<File[]>([]);
  const [isSending, setIsSending] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<EmailFormData>({
    resolver: zodResolver(emailSchema),
  });

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const newFiles = Array.from(e.target.files);
      setFiles(prev => [...prev, ...newFiles]);
    }
  };

  const removeFile = (index: number) => {
    setFiles(prev => prev.filter((_, i) => i !== index));
  };

  const onSubmit = async (data: EmailFormData) => {
    setIsSending(true);

    try {
      const formData = new FormData();
      formData.append("fromAddress", "no-reply@yourapp.com");
      formData.append("toAddress", data.to);
      formData.append("subject", data.subject);
      formData.append("body", data.message);

      if (files.length > 0) {
        formData.append("attachment", files[0]); // Send the actual file
      }

      const response = await fetch("http://localhost:8080/api/emails", {
        method: "POST",
        body: formData,
      });

      const result = await response.text();

      if (response.ok && !result.startsWith("Error")) {
        toast({
          title: "✓ Email Sent Successfully!",
          description: `Your email to ${data.to} has been sent.`,
          variant: "default",
        });
        reset();
        setFiles([]);
      } else {
        toast({
          title: "✗ Email Failed",
          description: result || "Something went wrong.",
          variant: "destructive",
        });
      }
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : "An unknown error occurred";
      toast({
        title: "✗ Network Error",
        description: errorMessage,
        variant: "destructive",
      });
    } finally {
      setIsSending(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-accent/20">
      <div className="container mx-auto px-4 py-8">
        <Button
          variant="ghost"
          onClick={() => navigate("/")}
          className="mb-6 hover:bg-accent"
        >
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Home
        </Button>

        <div className="max-w-3xl mx-auto">
          <Card className="shadow-[0_8px_24px_-4px_hsl(var(--foreground)/0.12)] border-border">
            <CardHeader>
              <CardTitle className="text-3xl">Compose Email</CardTitle>
              <CardDescription className="text-base">
                Fill in the details below to send your email
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                <div className="space-y-2">
                  <Label htmlFor="to">To</Label>
                  <Input
                    id="to"
                    type="email"
                    placeholder="recipient@example.com"
                    {...register("to")}
                    className={errors.to ? "border-destructive" : ""}
                  />
                  {errors.to && (
                    <p className="text-sm text-destructive">{errors.to.message}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="subject">Subject</Label>
                  <Input
                    id="subject"
                    placeholder="Enter email subject"
                    {...register("subject")}
                    className={errors.subject ? "border-destructive" : ""}
                  />
                  {errors.subject && (
                    <p className="text-sm text-destructive">{errors.subject.message}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="message">Message</Label>
                  <Textarea
                    id="message"
                    placeholder="Type your message here..."
                    rows={8}
                    {...register("message")}
                    className={errors.message ? "border-destructive" : ""}
                  />
                  {errors.message && (
                    <p className="text-sm text-destructive">{errors.message.message}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="attachment">Attachments</Label>
                  <div className="flex items-center gap-2">
                    <Input
                      id="attachment"
                      type="file"
                      multiple
                      onChange={handleFileChange}
                      className="hidden"
                    />
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => document.getElementById("attachment")?.click()}
                      className="w-full"
                    >
                      <Paperclip className="mr-2 h-4 w-4" />
                      Choose Files
                    </Button>
                  </div>
                  
                  {files.length > 0 && (
                    <div className="mt-4 space-y-2">
                      {files.map((file, index) => (
                        <div
                          key={index}
                          className="flex items-center justify-between p-3 rounded-lg bg-accent/50 border border-border"
                        >
                          <div className="flex items-center gap-2 flex-1 min-w-0">
                            <Paperclip className="h-4 w-4 text-muted-foreground flex-shrink-0" />
                            <span className="text-sm truncate">{file.name}</span>
                            <span className="text-xs text-muted-foreground flex-shrink-0">
                              ({(file.size / 1024).toFixed(1)} KB)
                            </span>
                          </div>
                          <Button
                            type="button"
                            variant="ghost"
                            size="sm"
                            onClick={() => removeFile(index)}
                            className="flex-shrink-0 hover:bg-destructive/10 hover:text-destructive"
                          >
                            <X className="h-4 w-4" />
                          </Button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                <Button
                  type="submit"
                  className="w-full bg-gradient-to-r from-primary to-primary-glow hover:shadow-[0_0_32px_hsl(var(--primary-glow)/0.4)] transition-all duration-300"
                  disabled={isSending}
                >
                  {isSending ? (
                    <>Sending...</>
                  ) : (
                    <>
                      <Send className="mr-2 h-4 w-4" />
                      Send Email
                    </>
                  )}
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default SendEmail;
