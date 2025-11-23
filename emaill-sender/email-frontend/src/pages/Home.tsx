import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";
import { Mail, Send, FileText, Zap } from "lucide-react";

const Home = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-accent/20">
      <div className="container mx-auto px-4 py-16">
        <div className="flex flex-col items-center justify-center min-h-[80vh] text-center">
          <div className="mb-8 inline-flex items-center justify-center w-20 h-20 rounded-2xl bg-gradient-to-br from-primary to-primary-glow shadow-[0_0_32px_hsl(var(--primary-glow)/0.4)] animate-pulse">
            <Mail className="w-10 h-10 text-primary-foreground" />
          </div>
          
          <h1 className="text-5xl md:text-7xl font-bold mb-6 bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
            Simple Email Service
          </h1>
          
          <p className="text-xl md:text-2xl text-muted-foreground mb-12 max-w-2xl">
            Send emails effortlessly with our clean, intuitive interface. 
            Fast, simple, and reliable.
          </p>

          <Button 
            onClick={() => navigate("/send")} 
            size="lg"
            className="text-lg px-8 py-6 bg-gradient-to-r from-primary to-primary-glow hover:shadow-[0_0_32px_hsl(var(--primary-glow)/0.4)] transition-all duration-300 hover:scale-105"
          >
            <Send className="mr-2 h-5 w-5" />
            Send an Email
          </Button>

          <div className="mt-24 grid grid-cols-1 md:grid-cols-3 gap-8 max-w-4xl w-full">
            <div className="p-6 rounded-2xl bg-card border border-border shadow-[0_2px_8px_-2px_hsl(var(--foreground)/0.08)] hover:shadow-[0_8px_24px_-4px_hsl(var(--foreground)/0.12)] transition-all duration-300">
              <div className="mb-4 inline-flex items-center justify-center w-12 h-12 rounded-xl bg-primary/10 text-primary">
                <Zap className="w-6 h-6" />
              </div>
              <h3 className="text-lg font-semibold mb-2">Lightning Fast</h3>
              <p className="text-sm text-muted-foreground">
                Send emails in seconds with our optimized interface
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-card border border-border shadow-[0_2px_8px_-2px_hsl(var(--foreground)/0.08)] hover:shadow-[0_8px_24px_-4px_hsl(var(--foreground)/0.12)] transition-all duration-300">
              <div className="mb-4 inline-flex items-center justify-center w-12 h-12 rounded-xl bg-primary/10 text-primary">
                <FileText className="w-6 h-6" />
              </div>
              <h3 className="text-lg font-semibold mb-2">File Attachments</h3>
              <p className="text-sm text-muted-foreground">
                Attach multiple files with ease and confidence
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-card border border-border shadow-[0_2px_8px_-2px_hsl(var(--foreground)/0.08)] hover:shadow-[0_8px_24px_-4px_hsl(var(--foreground)/0.12)] transition-all duration-300">
              <div className="mb-4 inline-flex items-center justify-center w-12 h-12 rounded-xl bg-primary/10 text-primary">
                <Mail className="w-6 h-6" />
              </div>
              <h3 className="text-lg font-semibold mb-2">Simple & Clean</h3>
              <p className="text-sm text-muted-foreground">
                No clutter, just the essentials you need
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;
