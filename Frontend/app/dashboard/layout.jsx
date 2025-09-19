"use client";
import { useEffect } from "react";
import { useRouter } from "next/navigation";
import Sidebar from "../../components/Sidebar";
import { auth } from "../../lib/auth";

export default function DashboardLayout({ children }) {
  const router = useRouter();

  useEffect(() => {
    // Check if user is authenticated
    if (!auth.isAuthenticated()) {
      router.push("/login");
      return;
    }

    // Check if token is expired
    if (auth.isTokenExpired()) {
      auth.logout();
      router.push("/login");
    }
  }, [router]);

  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar />
      <main className="flex-1 overflow-auto">
        <div className="p-6">{children}</div>
      </main>
    </div>
  );
}
