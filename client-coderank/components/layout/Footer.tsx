export default function Footer() {
  return (
    <footer className="border-t bg-background py-8 mt-auto">
      <div className="container mx-auto px-4 flex flex-col items-center justify-center gap-4">
        <p className="text-sm text-muted-foreground">
          © {new Date().getFullYear()} CodeScore. All rights reserved.
        </p>
      </div>
    </footer>
  );
}
