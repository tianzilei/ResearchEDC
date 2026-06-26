import "@testing-library/jest-dom";

const mockMatchMedia = (query: string) => ({
  matches: false,
  media: query,
  onchange: null,
  addListener: () => void 0,
  removeListener: () => void 0,
  addEventListener: () => void 0,
  removeEventListener: () => void 0,
  dispatchEvent: () => false,
});

Object.defineProperty(window, "matchMedia", {
  writable: true,
  value: mockMatchMedia,
});
