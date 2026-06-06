import { createContext, useContext, useState } from 'react'

const RefreshContext = createContext(null)

export function RefreshProvider({ children }) {
  const [refreshGlobal, setRefreshGlobal] = useState(0)

  function dispararRefresh() {
    setRefreshGlobal((n) => n + 1)
  }

  return (
    <RefreshContext.Provider value={{ refreshGlobal, dispararRefresh }}>
      {children}
    </RefreshContext.Provider>
  )
}

export function useRefresh() {
  return useContext(RefreshContext)
}
