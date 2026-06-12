import type { Metadata } from "next";
import { DM_Sans } from "next/font/google";
import "./globals.css";
import RootLayoutClient from "./RootLayoutClient";

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
        <RootLayoutClient>{children}</RootLayoutClient>
      </body>
    </html>
  );
}
