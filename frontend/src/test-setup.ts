import "@testing-library/jest-dom";
console.log("TEST_SETUP_LOADED");

const mockMatchMedia = (query: string) => ({
  matches: false,
  media: query,
  onchange: null,
  addListener: () => {},
  removeListener: () => {},
  addEventListener: () => {},
  removeEventListener: () => {},
  dispatchEvent: () => false,
});

Object.defineProperty(window, "matchMedia", {
  writable: true,
  value: mockMatchMedia,
});
console.log("MATCH_MEDIA_MOCKED");
