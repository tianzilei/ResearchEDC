import { lazy } from "react";
import { createBrowserRouter, Navigate } from "react-router-dom";
import AppLayout from "@/layouts/AppLayout";
import Login from "@/pages/Login";
import NotFound from "@/pages/NotFound";
import ErrorPage from "@/pages/ErrorPage";
import LegacyFrame from "@/components/LegacyFrame";

const Dashboard = lazy(() => import("@/pages/Dashboard"));
const RandomizationDashboard = lazy(() => import("@/pages/randomization/RandomizationDashboard"));
const SchemeEditor = lazy(() => import("@/pages/randomization/SchemeEditor"));
const AllocationPage = lazy(() => import("@/pages/randomization/AllocationPage"));
const UnblindingPage = lazy(() => import("@/pages/randomization/UnblindingPage"));
const AuditViewer = lazy(() => import("@/pages/randomization/AuditViewer"));
const ExportCenter = lazy(() => import("@/pages/export/ExportCenter"));
const CrfList = lazy(() => import("@/pages/crf/CrfList"));
const CrfPreview = lazy(() => import("@/pages/crf/CrfPreview"));
const SubjectList = lazy(() => import("@/pages/subject/SubjectList"));
const UserManagement = lazy(() => import("@/pages/admin/UserManagement"));
const AuditLogViewer = lazy(() => import("@/pages/admin/AuditLogViewer"));
const SystemConfiguration = lazy(() => import("@/pages/admin/SystemConfiguration"));
const AdminDashboard = lazy(() => import("@/pages/admin/AdminDashboard"));
const CrfAdmin = lazy(() => import("@/pages/admin/CrfAdmin"));
const SubjectDetail = lazy(() => import("@/pages/subject/SubjectDetail"));
const StudyList = lazy(() => import("@/pages/studies/StudyList"));
const StudyWizard = lazy(() => import("@/pages/studies/StudyWizard"));
const EventList = lazy(() => import("@/pages/events/EventList"));
const DataEntryPage = lazy(() => import("@/pages/datacapture/DataEntryPage"));
const RulesListPage = lazy(() => import("@/pages/rules/RulesListPage"));
const StudyDetail = lazy(() => import("@/pages/studies/StudyDetail"));
const StudyEditor = lazy(() => import("@/pages/studies/StudyEditor"));
const SiteManagement = lazy(() => import("@/pages/studies/SiteManagement"));
const EventDefinitionsPage = lazy(() => import("@/pages/studies/EventDefinitionsPage"));
const SubjectGroupsPage = lazy(() => import("@/pages/studies/SubjectGroupsPage"));
const JobManager = lazy(() => import("@/pages/admin/JobManager"));
const DatasetBuilder = lazy(() => import("@/pages/export/DatasetBuilder"));
const Profile = lazy(() => import("@/pages/Profile"));

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
        path: "data-export/datasets",
        element: <DatasetBuilder />,
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
        path: "studies",
        element: <StudyList />,
      },
      {
        path: "studies/create",
        element: <StudyWizard />,
      },
      {
        path: "studies/:id",
        element: <StudyDetail />,
      },
      {
        path: "studies/:id/edit",
        element: <StudyEditor />,
      },
      {
        path: "studies/:id/sites",
        element: <SiteManagement />,
      },
      {
        path: "studies/:studyId/event-definitions",
        element: <EventDefinitionsPage />,
      },
      {
        path: "studies/:studyId/subject-groups",
        element: <SubjectGroupsPage />,
      },
      {
        path: "studies/:studyId/rules",
        element: <RulesListPage />,
      },
      {
        path: "admin/users",
        element: <UserManagement />,
      },
      {
        path: "admin/audit-log",
        element: <AuditLogViewer />,
      },
      {
        path: "admin/system",
        element: <SystemConfiguration />,
      },
      {
        path: "admin",
        element: <AdminDashboard />,
      },
      {
        path: "admin/crf-library",
        element: <CrfAdmin />,
      },
      {
        path: "admin/jobs",
        element: <JobManager />,
      },
      {
        path: "subjects",
        element: <SubjectList />,
      },
      {
        path: "subjects/:id",
        element: <SubjectDetail />,
      },
      {
        path: "subjects/:subjectId/events",
        element: <EventList />,
      },
      {
        path: "subjects/:subjectId/events/:eventId/crfs/:eventCrfId/entry",
        element: <DataEntryPage />,
      },
      {
        path: "profile",
        element: <Profile />,
      },
      {
        path: "audit-log",
        element: <AuditViewer />,
      },
      {
        path: "403",
        element: <ErrorPage status={403} />,
      },
      {
        path: "legacy/*",
        element: <LegacyFrame />,
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
