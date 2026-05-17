import { lazy } from "react";
import { createBrowserRouter, Navigate } from "react-router-dom";
import AppLayout from "@/layouts/AppLayout";
import Login from "@/pages/Login";
import NotFound from "@/pages/NotFound";
import ErrorPage from "@/pages/ErrorPage";

const Dashboard = lazy(() => import("@/pages/Dashboard"));
const RandomizationDashboard = lazy(() => import("@/pages/randomization/RandomizationDashboard"));
const SchemeEditor = lazy(() => import("@/pages/randomization/SchemeEditor"));
const AllocationPage = lazy(() => import("@/pages/randomization/AllocationPage"));
const UnblindingPage = lazy(() => import("@/pages/randomization/UnblindingPage"));
const AuditViewer = lazy(() => import("@/pages/randomization/AuditViewer"));
const ExportCenter = lazy(() => import("@/pages/export/ExportCenter"));
const CrfList = lazy(() => import("@/pages/crf/CrfList"));
const CrfPreview = lazy(() => import("@/pages/crf/CrfPreview"));

const QuestionnaireTemplates = lazy(() => import("@/pages/questionnaire/QuestionnaireTemplates"));
const QuestionnaireVersionEditor = lazy(() => import("@/pages/questionnaire/QuestionnaireVersionEditor"));
const QuestionnaireAssignments = lazy(() => import("@/pages/questionnaire/QuestionnaireAssignments"));
const QuestionnaireResponses = lazy(() => import("@/pages/questionnaire/QuestionnaireResponses"));
const QuestionnaireExport = lazy(() => import("@/pages/questionnaire/QuestionnaireExport"));
const QuestionnaireFill = lazy(() => import("@/pages/questionnaire/QuestionnaireFill"));
const QuestionnaireMyTasks = lazy(() => import("@/pages/questionnaire/QuestionnaireMyTasks"));

const router = createBrowserRouter([
  {
    path: "/app",
    element: <AppLayout />,
    children: [
      {
        index: true,
        element: <Navigate to="dashboard" replace />,
      },
      {
        path: "dashboard",
        element: <Dashboard />,
      },
      {
        path: "randomization",
        element: <RandomizationDashboard />,
      },
      {
        path: "randomization/schemes/:id",
        element: <SchemeEditor />,
      },
      {
        path: "randomization/schemes/:id/allocate",
        element: <AllocationPage />,
      },
      {
        path: "randomization/schemes/:id/unblind",
        element: <UnblindingPage />,
      },
      {
        path: "randomization/schemes/:id/audit",
        element: <AuditViewer />,
      },
      {
        path: "data-export",
        element: <ExportCenter />,
      },
      {
        path: "crfs",
        element: <CrfList />,
      },
      {
        path: "crfs/:versionId",
        element: <CrfPreview />,
      },
      {
        path: "questionnaires/templates",
        element: <QuestionnaireTemplates />,
      },
      {
        path: "questionnaires/templates/:templateId/versions",
        element: <QuestionnaireVersionEditor />,
      },
      {
        path: "questionnaires/assignments",
        element: <QuestionnaireAssignments />,
      },
      {
        path: "questionnaires/responses",
        element: <QuestionnaireResponses />,
      },
      {
        path: "questionnaires/my-tasks",
        element: <QuestionnaireMyTasks />,
      },
      {
        path: "questionnaires/export",
        element: <QuestionnaireExport />,
      },
      {
        path: "audit-log",
        element: <AuditViewer />,
      },
      {
        path: "403",
        element: <ErrorPage status={403} />,
      },
    ],
  },
  {
    path: "/login",
    element: <Login />,
  },
  {
    path: "/q/fill/:token",
    element: <QuestionnaireFill />,
  },
  {
    path: "/error",
    element: <ErrorPage status={500} />,
  },
  {
    path: "/",
    element: <Navigate to="/app/dashboard" replace />,
  },
  {
    path: "*",
    element: <NotFound />,
  },
]);

export default router;
