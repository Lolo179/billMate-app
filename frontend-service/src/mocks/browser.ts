import { setupWorker } from "msw/browser";
import { handlersReact } from "@/mocks/handlersReact";

export const worker = setupWorker(...handlersReact);
