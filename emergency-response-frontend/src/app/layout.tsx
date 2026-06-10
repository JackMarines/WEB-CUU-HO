import type { Metadata } from "next";
import { DM_Sans } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/contexts/AuthContext";
import TopBar from "@/components/layout/TopBar";
import AppSidebar from "@/components/layout/AppSidebar";

const dmSans = DM_Sans({
  weight: ["400", "500", "600", "700"],
  subsets: ["latin", "latin-ext"],
  variable: "--font-sans",
});

export const metadata: Metadata = {
  title: "Cứu Trợ Khẩn Cấp",
  description: "Nền tảng kết nối người dân với trung tâm cứu trợ thiên tai",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi" className={`${dmSans.variable} h-full`}>
      <body className="h-screen flex flex-col overflow-hidden">
        <AuthProvider>
          <TopBar />
          <div className="flex flex-1 min-h-0">
            <AppSidebar />
            <main className="flex-1 overflow-auto">{children}</main>
          </div>
        </AuthProvider>
      </body>
    </html>
  );
}
